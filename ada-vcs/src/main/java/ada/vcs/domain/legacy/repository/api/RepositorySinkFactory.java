package ada.vcs.domain.legacy.repository.api;

import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import ada.vcs.domain.legacy.repository.api.version.VersionFactory;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySettings;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySettingsMemento;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySink;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySinkMemento;
import ada.vcs.domain.legacy.repository.watcher.WatcherSink;
import ada.vcs.domain.legacy.repository.watcher.WatcherSinkMemento;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class RepositorySinkFactory {

    private final ObjectMapper om;

    private final VersionFactory versionFactory;

    public RepositorySink create(RepositorySinkMemento memento) {
        if (memento instanceof FileSystemRepositorySinkMemento) {
            return createFileSystemRepositorySink((FileSystemRepositorySinkMemento) memento);
        } else if (memento instanceof WatcherSinkMemento) {
            return createWatcherSink((WatcherSinkMemento) memento);
        } else {
            String message = String.format("Unknown RepositorySinkMemento `%s`", memento.getClass());
            throw new IllegalArgumentException(message);
        }
    }

    public FileSystemRepositorySink createFileSystemRepositorySink(FileSystemRepositorySinkMemento memento) {
        final FileSystemRepositorySettingsMemento settingsMemento = memento.getSettings();
        final FileSystemRepositorySettings settings = FileSystemRepositorySettings.apply(om, settingsMemento);
        final VersionDetails details = versionFactory.createDetails(memento.getDetails());

        return FileSystemRepositorySink.apply(settings, memento.getRoot(), details);
    }

    public WatcherSink createWatcherSink(WatcherSinkMemento memento) {
        return WatcherSink.apply(create(memento.getActual()), memento.getRepositoryActor());
    }

}