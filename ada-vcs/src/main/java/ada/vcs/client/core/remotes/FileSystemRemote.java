package ada.vcs.client.core.remotes;

import ada.commons.util.FileSize;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.client.converters.internal.api.WriteSummary;
import ada.vcs.client.core.FileSystemDependent;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.alpakka.file.javadsl.LogRotatorSink;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileSystemRemote implements Remote, FileSystemDependent<FileSystemRemote> {

    private final ResourceName alias;

    private final Path dir;

    @JsonCreator
    public static FileSystemRemote apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("dir") Path dir) {

        if (Files.exists(dir) && !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path must be a directory, not a file");
        }

        return new FileSystemRemote(alias, dir);
    }

    @Override
    public FileSystemRemote resolve(Path to) {
        return apply(alias, to.resolve(dir));
    }

    @Override
    public FileSystemRemote relativize(Path to) {
        return apply(alias, to.relativize(dir));
    }

    @Override
    public String getInfo() {
        return dir.toString();
    }

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> push(Schema schema) {
        final FileSize maxChunkSize = FileSize.apply(10, FileSize.Unit.MEGABYTES);
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

        Creator<Function<ByteString, Optional<Path>>> rotationFunction = () -> {
            final long max = maxChunkSize.getBytes();
            final long[] size = new long[]{max};
            final int[] count = new int[]{0};

            return (element) -> {
                if (size[0] + element.size() > max) {
                    Path path = dir.resolve(String.format("records-%d.avro", ++count[0]));
                    size[0] = element.size();

                    Files.createFile(path);

                    return Optional.of(path);
                } else {
                    size[0] += element.size();
                    return Optional.empty();
                }
            };
        };

        return Flow
            .of(GenericRecord.class)
            .map(record -> (List<GenericRecord>) Lists.newArrayList(record))
            .statefulMapConcat(writeBytes)
            .via(Compression.gzip())
            .toMat(
                LogRotatorSink.createFromFunction(rotationFunction),
                Keep.right())
            .mapMaterializedValue(done -> done.thenApply(d -> WriteSummary.apply(42)));
    }

}
