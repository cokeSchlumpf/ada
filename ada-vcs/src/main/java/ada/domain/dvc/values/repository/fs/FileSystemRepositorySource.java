package ada.domain.dvc.values.repository.fs;

import ada.commons.util.Operators;
import ada.domain.dvc.values.repository.RefSpec;
import ada.domain.dvc.values.repository.RepositorySource;
import ada.domain.legacy.repository.api.exceptions.TagReferenceNotFoundException;
import ada.domain.legacy.repository.api.exceptions.VersionReferenceNotFoundException;
import ada.domain.dvc.values.repository.version.VersionDetails;
import ada.domain.dvc.values.repository.version.VersionFactory;
import ada.domain.legacy.repository.fs.FileSystemRepositorySettings;
import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@AllArgsConstructor(staticName = "apply")
public class FileSystemRepositorySource implements RepositorySource {

    private final FileSystemRepositorySettings settings;

    private final Path root;

    private final RefSpec.VersionRef refSpec;

    private final VersionFactory versionFactory;

    private final Materializer materializer;

    @Override
    public Source<ByteString, CompletionStage<VersionDetails>> get() {
        CompletionStage<Source<ByteString, VersionDetails>> data = refSpec
            .map(
                tagRef -> datasets()
                    .filter(version -> version
                        .tag()
                        .map(tag -> tag.alias().getValue().equals(tagRef.getAlias().getValue()))
                        .orElse(false))
                    .runWith(Sink.seq(), materializer)
                    .thenApply(List::stream)
                    .thenApply(Stream::findFirst)
                    .thenApply(tag -> tag.orElseThrow(() -> TagReferenceNotFoundException.apply(tagRef)))
                    .thenApply(VersionDetails::id)
                    .thenApply(RefSpec.VersionRef::apply),
                CompletableFuture::completedFuture)
            .thenApply(versionRef -> {
                final Path source = root.resolve(versionRef.getId());
                final Path detailsFile = source.resolve(settings.getDetailsFileName());

                final VersionDetails versionDetails = Operators.suppressExceptions(() -> {
                    try (InputStream is = Files.newInputStream(detailsFile)) {
                        return versionFactory.createDetails(is);
                    }
                });

                if (!Files.isDirectory(source)) {
                    throw VersionReferenceNotFoundException.apply(versionRef);
                }

                Iterator<Path> pathsIt = Operators
                    .suppressExceptions(() -> Files.newDirectoryStream(source, path -> path.toString().endsWith("avro")))
                    .iterator();

                ArrayList<Path> paths = Lists.newArrayList(pathsIt);
                paths.sort(Comparator.naturalOrder());

                return Operators.suppressExceptions(() -> Source
                    .from(paths)
                    .flatMapConcat(FileIO::fromPath)
                    .mapMaterializedValue(done -> versionDetails));
            });

        return Source
            .fromSourceCompletionStage(data)
            .mapMaterializedValue(s -> s);
    }

    public Source<VersionDetails, NotUsed> datasets() {
        return Operators.suppressExceptions(() -> {
            List<VersionDetails> details = Lists
                .newArrayList(Files
                    .newDirectoryStream(root)
                    .iterator())
                .stream()
                .map(version -> {
                    Path versionFile = version.resolve(settings.getDetailsFileName());

                    return Operators.suppressExceptions(() -> {
                        if (Files.exists(versionFile)) {
                            try (InputStream is = Files.newInputStream(versionFile)) {
                                return Optional.of(versionFactory.createDetails(is));
                            }
                        } else {
                            return Optional.<VersionDetails>empty();
                        }
                    });
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

            return Source.from(details);
        });
    }

}
