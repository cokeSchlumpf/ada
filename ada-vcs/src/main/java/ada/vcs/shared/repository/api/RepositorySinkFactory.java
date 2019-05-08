package ada.vcs.shared.repository.api;

import ada.vcs.shared.repository.api.version.VersionDetails;
import ada.vcs.shared.repository.api.version.VersionFactory;
import ada.vcs.shared.repository.fs.FileSystemRepositorySettings;
import ada.vcs.shared.repository.fs.FileSystemRepositorySettingsMemento;
import ada.vcs.shared.repository.fs.FileSystemRepositorySink;
import ada.vcs.shared.repository.fs.FileSystemRepositorySinkMemento;
import ada.vcs.shared.repository.watcher.WatcherSink;
import ada.vcs.shared.repository.watcher.WatcherSinkMemento;
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
