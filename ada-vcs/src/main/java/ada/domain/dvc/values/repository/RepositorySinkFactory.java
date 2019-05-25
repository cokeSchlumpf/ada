package ada.domain.dvc.values.repository;

import ada.domain.dvc.values.repository.version.VersionDetails;
import ada.domain.dvc.values.repository.version.VersionFactory;
import ada.adapters.cli.repository.fs.FileSystemRepositorySettings;
import ada.adapters.cli.repository.fs.FileSystemRepositorySettingsMemento;
import ada.domain.dvc.values.repository.fs.FileSystemRepositorySink;
import ada.domain.dvc.values.repository.fs.FileSystemRepositorySinkMemento;
import ada.domain.dvc.values.repository.watcher.WatcherSink;
import ada.domain.dvc.values.repository.watcher.WatcherSinkMemento;
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
