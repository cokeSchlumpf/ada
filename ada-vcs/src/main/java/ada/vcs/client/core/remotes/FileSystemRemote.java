package ada.vcs.client.core.remotes;

import ada.commons.util.FileSize;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.Repository;
import ada.vcs.client.core.repository.api.User;
import ada.vcs.client.core.repository.api.version.Tag;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.NotUsed;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.alpakka.file.javadsl.LogRotatorSink;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class FileSystemRemote implements Remote {

    private final ObjectMapper om;

    private final ResourceName alias;

    private final Path dir;

    private final Repository delegate;

    public static FileSystemRemote apply(ObjectMapper om, ResourceName alias, Path dir, Repository delegate) {
        if (Files.exists(dir) && !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path must be a directory, not a file");
        }

        return new FileSystemRemote(om, alias, dir, delegate);
    }

    public static FileSystemRemote apply(ObjectMapper om, FileSystemRemoteMemento memento, Repository delegate) {
        return apply(om, memento.getAlias(), memento.getDir(), delegate);
    }

    @Override
    public ResourceName alias() {
        return alias;
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
    public String info() {
        return dir.toString();
    }

    @Override
    public RemoteMemento memento() {
        return FileSystemRemoteMemento.apply(alias, dir);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, FileSystemRemoteMemento.apply(alias, dir));
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
            .mapMaterializedValue(done -> done.thenApply(d -> null)); // TODO
    }

    @Override
    public Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec) {
        return null;
    }

}
