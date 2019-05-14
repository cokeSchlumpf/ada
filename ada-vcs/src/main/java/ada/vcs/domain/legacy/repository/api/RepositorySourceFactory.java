package ada.vcs.domain.legacy.repository.api;

import ada.vcs.domain.legacy.repository.api.version.VersionFactory;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySettings;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySource;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySourceMemento;
import ada.vcs.domain.legacy.repository.watcher.WatcherSource;
import ada.vcs.domain.legacy.repository.watcher.WatcherSourceMemento;
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
