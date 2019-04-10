package ada.vcs.client.converters.local;

import ada.commons.io.FilenameTemplate;
import ada.commons.io.RotatingFileOutputStream;
import ada.commons.io.RotationConfig;
import ada.commons.util.FileSize;
import ada.vcs.client.converters.internal.api.DataSink;
import ada.vcs.client.converters.internal.api.WriteSummary;
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
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public class LocalSink implements DataSink {

    private final Path directory;

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema) {
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);

        RotationConfig config = RotationConfig.apply(
            directory,
            FilenameTemplate.apply("records", "avro"),
            FileSize.apply(1, FileSize.Unit.GIGABYTES),
            FileSize.apply(100, FileSize.Unit.MEGABYTES));

        try (RotatingFileOutputStream fos = RotatingFileOutputStream.apply(config)) {
            dataFileWriter.create(schema, fos);

            return Flow
                .of(GenericRecord.class)
                .toMat(
                    Sink.fold(dataFileWriter, (writer, record) -> {
                        writer.append(record);
                        return writer;
                    }),
                    (notUsed, cs) -> CompletableFuture.completedFuture(WriteSummary.apply(42L)));
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    @Override
    public String getInfo() {
        return "local";
    }

}
