package ada.vcs.client.converters.avro;

import ada.vcs.client.converters.api.DataSink;
import ada.vcs.client.converters.api.DataSinkMemento;
import ada.vcs.client.converters.api.WriteSummary;
import ada.vcs.client.core.FileSystemDependent;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
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

@AllArgsConstructor(staticName = "apply")
public final class AvroSink implements DataSink, FileSystemDependent<AvroSink> {

    private final Path path;

    public static AvroSink apply(AvroSinkMemento memento) {
        return apply(memento.getPath());
    }

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema) {
        final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);

        try {
            final FileOutputStream fos = new FileOutputStream(path.toFile());
            final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
            DataFileWriter<GenericRecord> writer = dataFileWriter.create(schema, fos);

            return Flow
                .of(GenericRecord.class)
                .map(record -> {
                    writer.append(record);
                    return record;
                })
                .toMat(
                    Sink.fold(0L, (count, record) -> count + 1),
                    (notUsed, count) -> count
                        .thenApply(c -> {
                            try {
                                writer.flush();
                                writer.close();
                                fos.close();

                                return c;
                            } catch (IOException e) {
                                return ExceptionUtils.wrapAndThrow(e);
                            }
                        })
                        .thenApply(WriteSummary::apply));
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    @Override
    public String info() {
        return String.format("avro(%s)", path);
    }

    @Override
    public DataSinkMemento memento() {
        return null;
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
