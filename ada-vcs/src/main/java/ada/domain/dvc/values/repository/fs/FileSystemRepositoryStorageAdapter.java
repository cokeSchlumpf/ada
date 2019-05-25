package ada.domain.dvc.values.repository.fs;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.domain.dvc.values.repository.RefSpec;
import ada.domain.dvc.values.repository.RepositorySinkMemento;
import ada.domain.dvc.values.repository.RepositorySourceMemento;
import ada.domain.dvc.values.repository.RepositoryStorageAdapter;
import ada.domain.dvc.values.repository.version.VersionDetails;
import ada.domain.legacy.repository.fs.FileSystemRepositorySettings;
import akka.Done;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class FileSystemRepositoryStorageAdapter implements RepositoryStorageAdapter {

    private final FileSystemRepositorySettings settings;

    private final Path root;

    @Override
    public CompletionStage<Done> clean(ResourceName namespace, ResourceName repository) {
        return CompletableFuture.supplyAsync(() -> {
            Operators.suppressExceptions(() -> FileUtils
                .deleteDirectory(dataPath(namespace, repository).toFile()));
            return Done.getInstance();
        });
    }

    @Override
    public CompletionStage<Done> clean(ResourceName namespace, ResourceName repository, RefSpec.VersionRef version) {
        return CompletableFuture.supplyAsync(() -> {
            Operators.suppressExceptions(() -> FileUtils
                .deleteDirectory(dataPath(namespace, repository).resolve(version.getId()).toFile()));
            return Done.getInstance();
        });
    }

    @Override
    public RepositorySinkMemento push(ResourceName namespace, ResourceName repository, VersionDetails version) {
        return FileSystemRepositorySinkMemento.apply(
            dataPath(namespace, repository), settings.memento(), version.memento());
    }

    @Override
    public RepositorySourceMemento pull(ResourceName namespace, ResourceName repository, RefSpec.VersionRef version) {
        return FileSystemRepositorySourceMemento.apply(
            dataPath(namespace, repository), settings.memento(), version);
    }

    private Path dataPath(ResourceName namespace, ResourceName repository) {
        return root.resolve(namespace.getValue()).resolve(repository.getValue());
    }

}
