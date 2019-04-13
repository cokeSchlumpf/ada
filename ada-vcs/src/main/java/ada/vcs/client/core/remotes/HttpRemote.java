package ada.vcs.client.core.remotes;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.client.converters.internal.api.WriteSummary;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.javadsl.Compression;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRemote implements Remote, RemoteProperties {

    private final ResourceName alias;

    private final URL endpoint;

    @JsonCreator
    public static HttpRemote apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("endpoint") URL endpoint) {

        return new HttpRemote(alias, endpoint);
    }

    @Override
    public String getInfo() {
        return endpoint.toString();
    }

    @Override
    public RemoteProperties getProperties() {
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

}
