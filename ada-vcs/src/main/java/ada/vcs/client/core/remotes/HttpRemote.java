package ada.vcs.client.core.remotes;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.User;
import ada.vcs.client.core.repository.api.version.Tag;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.reactivestreams.Publisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(staticName = "apply")
final class HttpRemote implements Remote {

    private final ObjectMapper om;

    private final ActorSystem system;

    private final ResourceName alias;

    private final URL endpoint;

    public static HttpRemote apply(ObjectMapper om, ActorSystem system, HttpRemoteMemento memento) {
        return HttpRemote.apply(om, system, memento.getAlias(), memento.getEndpoint());
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
    public Source<VersionDetails, NotUsed> history() {
        return null;
    }

    @Override
    public Sink<GenericRecord, CompletionStage<VersionDetails>> push(Schema schema, User user) {
        final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);

        Creator<Function<List<GenericRecord>, Iterable<ByteString>>> writeBytes = () -> {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
            DataFileWriter<GenericRecord> writer = dataFileWriter.create(schema, baos);

            return records -> records
                .stream()
                .map(record -> {
                    baos.reset();
                    Operators.suppressExceptions(() -> {
                        writer.append(record);
                        writer.flush();
                    });

                    return ByteString.fromArray(baos.toByteArray());
                })
                .collect(Collectors.toList());
        };

        Function<Publisher<ByteString>, CompletionStage<HttpResponse>> consumer = publisher -> {
            HttpEntity.Chunked entity = HttpEntities.create(
                ContentTypes.APPLICATION_OCTET_STREAM,
                Source.fromPublisher(publisher));

            return Http
                .get(system)
                .singleRequest(HttpRequest.PUT(endpoint.toString()).withEntity(entity));
        };

        return Flow
            .of(GenericRecord.class)
            .map(record -> (List<GenericRecord>) Lists.newArrayList(record))
            .statefulMapConcat(writeBytes)
            .via(Compression.gzip())
            .toMat(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), Keep.right())
            .mapMaterializedValue(consumer::apply)
            .mapMaterializedValue(response -> response.thenApply(i -> null)); // TODO: Correct response
    }

    @Override
    public Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec) {
        return null;
    }

}
