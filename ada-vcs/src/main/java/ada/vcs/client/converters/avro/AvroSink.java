package ada.vcs.client.converters.avro;

import ada.vcs.client.converters.internal.api.DataSink;
import ada.vcs.client.converters.internal.api.WriteSummary;
import ada.vcs.client.core.FileSystemDependent;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AvroSink implements DataSink, FileSystemDependent<AvroSink> {

    @JsonProperty("path")
    private final Path path;

    @JsonCreator
    public static AvroSink apply(@JsonProperty("path") Path path) {
        return new AvroSink(path);
    }

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema) {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
            final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);

            dataFileWriter.create(schema, fos);

            return Flow
                .of(GenericRecord.class)
                .map(record -> {
                    dataFileWriter.append(record);
                    return record;
                })
                .toMat(
                    Sink.fold(0L, (count, record) -> count + 1),
                    (notUsed, count) -> count.thenApply(WriteSummary::apply));
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    @Override
    public String getInfo() {
        return String.format("avro(%s)", path);
    }

    @Override
    public AvroSink resolve(Path to) {
        return apply(to.resolve(path));
    }

    @Override
    public AvroSink relativize(Path to) {
        return apply(to.relativize(path));
    }

}
