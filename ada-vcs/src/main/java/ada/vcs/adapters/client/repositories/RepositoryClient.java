package ada.vcs.adapters.client.repositories;

import ada.commons.util.Either;
import ada.commons.util.Operators;
import ada.vcs.adapters.client.modifiers.RequestModifier;
import ada.vcs.domain.dvc.protocol.queries.RepositoryDetailsResponse;
import ada.vcs.domain.dvc.values.Authorization;
import ada.vcs.domain.dvc.values.GrantedAuthorization;
import ada.vcs.domain.legacy.repository.api.RefSpec;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import ada.vcs.domain.legacy.repository.api.version.VersionFactory;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
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
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class RepositoryClient {

    private final URL endpoint;

    private final ActorSystem system;

    private final Materializer materializer;

    private final ObjectMapper om;

    private final VersionFactory versionFactory;

    private final RequestModifier modifier;

    public CompletionStage<RepositoryDetailsResponse> details() {
        return modifier
            .modifyClient(Http.get(system))
            .singleRequest(modifier.modifyRequest(HttpRequest.GET(endpoint.toString())))
            .thenCompose(response -> Jackson
                .unmarshaller(om, RepositoryDetailsResponse.class)
                .unmarshal(response.entity().withoutSizeLimit(), materializer));
    }

    public CompletionStage<GrantedAuthorization> grant(Authorization authorization) {
        URL repoUrl = Operators.suppressExceptions(() -> new URL(endpoint + "/access"));

        HttpEntity.Strict entity = HttpEntities.create(
            ContentTypes.APPLICATION_JSON,
            Operators.suppressExceptions(() -> om.writeValueAsBytes(authorization)));

        return modifier
            .modifyClient(Http.get(system))
            .singleRequest(modifier.modifyRequest(HttpRequest.PUT(repoUrl.toString()).withEntity(entity)))
            .thenCompose(response -> Jackson
                .unmarshaller(om, GrantedAuthorization.class)
                .unmarshal(response.entity().withoutSizeLimit(), materializer));
    }

    public Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec) {
        CompletableFuture<VersionDetails> mat = new CompletableFuture<>();

        InputStream is = Source
            .fromCompletionStage(
                modifier
                    .modifyClient(Http.get(system))
                    .singleRequest(modifier.modifyRequest(HttpRequest.GET(endpoint + "/" + refSpec.toString()))))
            .mapAsync(1, response -> Unmarshaller
                .entityToMultipartFormData()
                .unmarshal(response.entity().withoutSizeLimit(), materializer))
            .flatMapConcat(Multipart.FormData::getParts)
            .map(i -> ((Multipart.FormData.BodyPart) i))
            .<Source<Either<VersionDetails, ByteString>, NotUsed>>map(bodyPart -> {
                switch (bodyPart.getName()) {
                    case "details":
                        CompletableFuture<VersionDetails> versionDetailsCS = bodyPart
                            .getEntity()
                            .getDataBytes()
                            .runFold(ByteString.empty(), ByteString::concat, materializer)
                            .toCompletableFuture()
                            .thenApply(ByteString::toByteBuffer)
                            .thenApply(ByteBuffer::array)
                            .thenApply(bytes -> Operators.suppressExceptions(() -> versionFactory.createDetails(bytes)));

                        return Source
                            .fromCompletionStage(versionDetailsCS)
                            .map(Either::left);

                    case "records":
                        return bodyPart
                            .getEntity()
                            .getDataBytes()
                            .mapMaterializedValue(o -> NotUsed.getInstance())
                            .map(Either::right);

                    default:
                        return Source.empty();
                }
            })
            .flatMapConcat(s -> s)
            .filter(element -> element.map(
                versionDetails -> {
                    mat.complete(versionDetails);
                    return false;
                },
                bs -> true))
            .map(element -> element.map(
                versionDetails -> ByteString.empty(),
                bs -> bs))
            .via(Compression.gunzip(8192))
            .runWith(StreamConverters.asInputStream(), materializer);


        return Source
            .fromCompletionStage(mat)
            .flatMapConcat(details -> {
                final DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(details.schema());
                final DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(is, datumReader);

                return Source.from(dataFileStream);
            })
            .watchTermination(Keep.right())
            .mapMaterializedValue(done -> done.thenCompose(i -> mat));
    }

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

            return modifier
                .modifyClient(Http.get(system))
                .singleRequest(modifier.modifyRequest(HttpRequest.POST(endpoint.toString()).withEntity(formData.toEntity())));
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

    public CompletionStage<GrantedAuthorization> revoke(Authorization authorization) {
        URL repoUrl = Operators.suppressExceptions(() -> new URL(endpoint + "/access"));

        HttpEntity.Strict entity = HttpEntities.create(
            ContentTypes.APPLICATION_JSON,
            Operators.suppressExceptions(() -> om.writeValueAsBytes(authorization)));

        return modifier
            .modifyClient(Http.get(system))
            .singleRequest(modifier.modifyRequest(HttpRequest.DELETE(repoUrl.toString()).withEntity(entity)))
            .thenCompose(response -> Jackson
                .unmarshaller(om, GrantedAuthorization.class)
                .unmarshal(response.entity().withoutSizeLimit(), materializer));
    }

    public URL getEndpoint() {
        return endpoint;
    }

}
