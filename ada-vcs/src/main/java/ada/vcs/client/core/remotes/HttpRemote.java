package ada.vcs.client.core.remotes;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.User;
import ada.vcs.client.core.repository.api.version.Tag;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.client.core.repository.api.version.VersionFactory;
import ada.vcs.server.directives.RecordsUploadDataCollection;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.reactivestreams.Publisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Value
@AllArgsConstructor(staticName = "apply")
final class HttpRemote implements Remote {

    private final ObjectMapper om;

    private final ActorSystem system;

    private final Materializer materializer;

    private final VersionFactory versionFactory;

    private final ResourceName alias;

    private final URL endpoint;

    public static HttpRemote apply(ObjectMapper om, ActorSystem system, Materializer materializer, VersionFactory versionFactory, HttpRemoteMemento memento) {
        return HttpRemote.apply(om, system, materializer, versionFactory, memento.getAlias(), memento.getEndpoint());
    }

    @Override
    public ResourceName alias() {
        return alias;
    }

    @Override
    public String info() {
        return endpoint.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Remote) {
            return memento().equals(((Remote) obj).memento());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return memento().hashCode();
    }

    @Override
    public RemoteMemento memento() {
        return HttpRemoteMemento.apply(alias, endpoint);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, HttpRemoteMemento.apply(alias, endpoint));
    }

    @Override
    public CompletionStage<RefSpec.TagRef> tag(User user, RefSpec.VersionRef ref, ResourceName name) {
        return null;
    }

    @Override
    public Source<Tag, NotUsed> tags() {
        return null;
    }

    @Override
    public Source<VersionDetails, NotUsed> datasets() {
        return null;
    }

    @Override
    public Sink<GenericRecord, CompletionStage<VersionDetails>> push(VersionDetails details) {
        Schema schema = details.schema();

        Creator<Function<List<GenericRecord>, Iterable<ByteString>>> writeBytes = () -> {
            final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);

            DataFileWriter<GenericRecord> writer = dataFileWriter.create(schema, baos);

            return records -> {
                records.forEach(record -> Operators.suppressExceptions(() -> writer.append(record)));
                writer.flush();
                ByteString result = ByteString.fromArray(baos.toByteArray());
                baos.reset();

                return Lists.newArrayList(result);
            };
        };

        Function<Publisher<ByteString>, CompletionStage<HttpResponse>> consumer = publisher -> {
            HttpEntity.Strict detailsField = HttpEntities.create(details.writeToString());

            HttpEntity.IndefiniteLength records = HttpEntities.createIndefiniteLength(
                ContentTypes.APPLICATION_OCTET_STREAM,
                Source.fromPublisher(publisher));

            Multipart.FormData formData = Multiparts.createFormDataFromParts(
                Multiparts.createFormDataBodyPart("details", detailsField),
                Multiparts.createFormDataBodyPart("records", records));

            return Http
                .get(system)
                .singleRequest(HttpRequest.PUT(endpoint.toString()).withEntity(formData.toEntity()));
        };

        return Flow
            .of(GenericRecord.class)
            .grouped(128)
            .statefulMapConcat(writeBytes)
            .via(Compression.gzip())
            .toMat(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), Keep.right())
            .mapMaterializedValue(consumer::apply)
            .mapMaterializedValue(response -> response
                .thenApply(r -> {
                    InputStream is = r
                        .entity()
                        .getDataBytes()
                        .runWith(StreamConverters.asInputStream(), materializer);

                    return Operators.suppressExceptions(() -> versionFactory.createDetails(is));
                }));
    }

    @Override
    public Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec) {
        CompletionStage<Source<GenericRecord, CompletionStage<VersionDetails>>> sourceCS = Http
            .get(system)
            .singleRequest(HttpRequest.GET(endpoint + "/" + refSpec.toString()))
            .thenCompose(response -> Unmarshaller
                .entityToMultipartFormData()
                .unmarshal(
                    response
                        .entity()
                        .withoutSizeLimit(),
                    materializer))
            .thenCompose(formData -> formData
                .getParts()
                .map(i -> ((Multipart.FormData.BodyPart) i))
                .foldAsync(RecordsUploadDataCollection.<Source<GenericRecord, CompletionStage<VersionDetails>>>empty(), (acc, bodyPart) -> {
                    switch (bodyPart.getName()) {
                        case "records":
                            return acc
                                .process(details -> {
                                    InputStream is = bodyPart
                                        .getEntity()
                                        .getDataBytes()
                                        .runWith(StreamConverters.asInputStream(), materializer);

                                    final DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(details.schema());
                                    final DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(is, datumReader);

                                    Source<GenericRecord, CompletionStage<VersionDetails>> genericRecordObjectSource = Source
                                        .from(dataFileStream)
                                        .watchTermination(Keep.right())
                                        .mapMaterializedValue(done -> done.thenApply(ignore -> details));

                                    return CompletableFuture.completedFuture(genericRecordObjectSource);
                                });

                        case "details":
                            return bodyPart
                                .getEntity()
                                .getDataBytes()
                                .runFold(ByteString.empty(), ByteString::concat, materializer)
                                .toCompletableFuture()
                                .thenApply(ByteString::toByteBuffer)
                                .thenApply(ByteBuffer::array)
                                .thenApply(bytes -> Operators.suppressExceptions(() -> versionFactory.createDetails(bytes)))
                                .thenApply(acc::withDetails);

                        default:
                            bodyPart.getEntity().discardBytes(materializer);
                            return CompletableFuture.completedFuture(acc);
                    }
                })
                .runWith(Sink.head(), materializer)
                .thenApply(result -> result.result().orElseThrow(() -> new RuntimeException("Something went wrong ..."))));

        return Source
            .fromSourceCompletionStage(sourceCS)
            .mapMaterializedValue(cs -> cs.thenCompose(vd -> vd));
    }

}
