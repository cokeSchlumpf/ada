package ada.vcs.client.core.repository.fs;

import ada.commons.util.Operators;
import ada.vcs.client.core.repository.api.version.VersionFactory;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class FileSystemRepositoryFactory {

    private static final String SETTINGS = "settings.json";

    private final ObjectMapper om;

    private final Materializer materializer;

    private final VersionFactory versionFactory;

    public FileSystemRepositoryImpl create(Path root) {
        Path settingsFile = root.resolve(SETTINGS);
        FileSystemRepositorySettings settings;

        if (Files.exists(settingsFile)) {
            settings = Operators.suppressExceptions(() -> {
                try (InputStream is = Files.newInputStream(root.resolve(SETTINGS))) {
                    return createSettings(root, is);
                }
            });
        } else {
            settings = createSettingsBuilder(root).build();
        }

        return create(settings);
    }

    public FileSystemRepositoryImpl create(FileSystemRepositorySettings settings) {
        if (!Files.exists(settings.getRoot())) {
            Operators.suppressExceptions(() -> {
                Files.createDirectories(settings.getRoot());
            });
        }

        Path settingsFile = settings.getRoot().resolve(SETTINGS);
        if (!Files.exists(settingsFile)) {
            Operators.suppressExceptions(() -> {
                try (OutputStream os = Files.newOutputStream(settingsFile)) {
                    settings.writeTo(os);
                }
            });
        }

        return FileSystemRepositoryImpl.apply(settings, versionFactory, materializer);
    }

    public FileSystemRepositorySettings.Builder createSettingsBuilder(Path root) {
        return FileSystemRepositorySettings.builder(root, om);
    }

    public FileSystemRepositorySettings createSettings(Path root, FileSystemRepositorySettingsMemento memento) {
        return FileSystemRepositorySettings.apply(
            root, om, memento.getBatchSize(), memento.getDetailsFilename(),
            memento.getMaxFileSize(), memento.getRecordsFilenameTemplate());
    }

    public FileSystemRepositorySettings createSettings(Path root, InputStream is) throws IOException {
        return createSettings(root, om.readValue(is, FileSystemRepositorySettingsMemento.class));
    }

}
