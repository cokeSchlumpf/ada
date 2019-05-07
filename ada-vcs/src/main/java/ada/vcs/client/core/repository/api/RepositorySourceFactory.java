package ada.vcs.client.core.repository.api;

import ada.vcs.client.core.repository.api.version.VersionFactory;
import ada.vcs.client.core.repository.fs.FileSystemRepositorySettings;
import ada.vcs.client.core.repository.fs.FileSystemRepositorySource;
import ada.vcs.client.core.repository.fs.FileSystemRepositorySourceMemento;
import ada.vcs.client.core.repository.watcher.WatcherSource;
import ada.vcs.client.core.repository.watcher.WatcherSourceMemento;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class RepositorySourceFactory {

    private final ObjectMapper om;

    private final VersionFactory versionFactory;

    private final Materializer materializer;

    public RepositorySource create(RepositorySourceMemento memento) {
        if (memento instanceof FileSystemRepositorySourceMemento) {
            return createFileSystemRepositorySource((FileSystemRepositorySourceMemento) memento);
        } else if (memento instanceof WatcherSourceMemento) {
            return createWatcherSource((WatcherSourceMemento) memento);
        } else {
            String message = String.format("Unknown RepositorySourceMemento '%s'", memento.getClass());
            throw new IllegalArgumentException(message);
        }
    }

    public FileSystemRepositorySource createFileSystemRepositorySource(FileSystemRepositorySourceMemento memento) {
        FileSystemRepositorySettings settings = FileSystemRepositorySettings.apply(om, memento.getSettings());
        return FileSystemRepositorySource.apply(settings, memento.getRoot(), memento.getVersion(), versionFactory, materializer);
    }

    public WatcherSource createWatcherSource(WatcherSourceMemento memento) {
        return WatcherSource.apply(create(memento.getActual()), memento.getRepositoryActor());
    }

}
