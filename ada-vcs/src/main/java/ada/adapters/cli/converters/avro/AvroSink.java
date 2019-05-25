package ada.adapters.cli.converters.avro;

import ada.adapters.cli.converters.api.DataSink;
import ada.adapters.cli.converters.api.WriteSummary;
import ada.adapters.cli.converters.api.DataSinkMemento;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class AvroSink implements DataSink {

    private final Path path;

    public static AvroSink apply(AvroSinkMemento memento) {
        return apply(memento.getPath());
    }

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema) {
        try {
            final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
            final OutputStream fos = Files.newOutputStream(path);
            final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
            final DataFileWriter<GenericRecord> writer = dataFileWriter.create(schema, fos);

            return Flow
                .of(GenericRecord.class)
                .map(records -> {
                    writer.append(records);
                    return records;
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
        return AvroSinkMemento.apply(path);
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
