package ada.vcs.shared.repository.fs;

import ada.commons.util.Operators;
import ada.vcs.shared.repository.api.RefSpec;
import ada.vcs.shared.repository.api.RepositorySink;
import ada.vcs.shared.repository.api.exceptions.VersionAlreadyExistsException;
import ada.vcs.shared.repository.api.version.VersionDetails;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.alpakka.file.javadsl.LogRotatorSink;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class FileSystemRepositorySink implements RepositorySink {

    private final FileSystemRepositorySettings settings;

    private final Path root;

    private final VersionDetails details;

    @Override
    public Sink<ByteString, CompletionStage<VersionDetails>> get() {
        return Operators.suppressExceptions(() -> {
            final Path target = root.resolve(details.id());
            final Path detailsFile = target.resolve(settings.getDetailsFileName());

            if (Files.exists(target)) {
                throw VersionAlreadyExistsException.apply(RefSpec.VersionRef.apply(details.id()));
            } else {
                Files.createDirectories(target);
            }

            try (OutputStream fos = Files.newOutputStream(detailsFile)) {
                details.writeTo(fos);
            }

            Creator<Function<ByteString, Optional<Path>>> rotationFunction = () -> {
                final long max = settings.getMaxFileSize().getBytes();
                final long[] size = new long[]{max};
                final int[] count = new int[]{0};

                return (element) -> {
                    if (size[0] + element.size() > max) {
                        String fileName = String.format(settings.getRecordsFileNameTemplate(), ++count[0]);
                        Path path = target.resolve(fileName);
                        size[0] = element.size();

                        Files.createFile(path);

                        return Optional.of(path);
                    } else {
                        size[0] += element.size();
                        return Optional.empty();
                    }
                };
            };

            return Flow.of(ByteString.class)
                .toMat(
                    LogRotatorSink.createFromFunction(rotationFunction),
                    Keep.right())
                .mapMaterializedValue(done -> done.thenApply(d -> details));
        });
    }

}
