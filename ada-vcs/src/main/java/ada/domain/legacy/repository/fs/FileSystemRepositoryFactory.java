package ada.domain.legacy.repository.fs;

import ada.commons.util.Operators;
import ada.domain.dvc.values.repository.version.VersionFactory;
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
            if (!Files.isDirectory(root)) {
                throw new IllegalArgumentException("Path must be a directory, not a file");
            }

            settings = Operators.suppressExceptions(() -> {
                try (InputStream is = Files.newInputStream(root.resolve(SETTINGS))) {
                    return createSettings(is);
                }
            });
        } else {
            settings = createSettingsBuilder().build();
        }

        return create(root, settings);
    }

    public FileSystemRepositoryImpl create(Path root, FileSystemRepositorySettings settings) {
        if (!Files.exists(root)) {
            Operators.suppressExceptions(() -> {
                Files.createDirectories(root);
            });
        }

        Path settingsFile = root.resolve(SETTINGS);
        if (!Files.exists(settingsFile)) {
            Operators.suppressExceptions(() -> {
                try (OutputStream os = Files.newOutputStream(settingsFile)) {
                    settings.writeTo(os);
                }
            });
        }

        return FileSystemRepositoryImpl.apply(settings, root, versionFactory, materializer);
    }

    public FileSystemRepositorySettings.Builder createSettingsBuilder() {
        return FileSystemRepositorySettings.builder(om);
    }

    public FileSystemRepositorySettings createSettings(FileSystemRepositorySettingsMemento memento) {
        return FileSystemRepositorySettings.apply(om, memento);
    }

    public FileSystemRepositorySettings createSettings(InputStream is) throws IOException {
        return createSettings(om.readValue(is, FileSystemRepositorySettingsMemento.class));
    }

}
