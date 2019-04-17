package ada.vcs.client.core.remotes;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.WriteSummary;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.javadsl.Compression;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(staticName = "apply")
final class HttpRemote implements Remote, RemoteMemento {

    private final ObjectMapper om;

    private final ResourceName alias;

    private final URL endpoint;

    public static HttpRemote apply(ObjectMapper om, HttpRemoteMemento memento) {
        return HttpRemote.apply(om, memento.getAlias(), memento.getEndpoint());
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
        return this;
    }

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> push(Schema schema) {
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

        return Flow
            .of(GenericRecord.class)
            .map(record -> (List<GenericRecord>) Lists.newArrayList(record))
            .statefulMapConcat(writeBytes)
            .via(Compression.gzip())
            .toMat(Sink.ignore(), Keep.right())
            .mapMaterializedValue(done -> done.thenApply(d -> WriteSummary.apply(42)));
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, HttpRemoteMemento.apply(alias, endpoint));
    }

    @Override
    public Remote resolve(Path to) {
        return this;
    }

    @Override
    public Remote relativize(Path to) {
        return this;
    }

}
