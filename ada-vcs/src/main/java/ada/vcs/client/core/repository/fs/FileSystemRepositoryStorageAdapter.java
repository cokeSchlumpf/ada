package ada.vcs.client.core.repository.fs;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.RepositorySourceMemento;
import ada.vcs.client.core.repository.api.RepositoryStorageAdapter;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.client.core.repository.api.RepositorySinkMemento;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class FileSystemRepositoryStorageAdapter implements RepositoryStorageAdapter {

    private final FileSystemRepositorySettings settings;

    private final Path root;

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
