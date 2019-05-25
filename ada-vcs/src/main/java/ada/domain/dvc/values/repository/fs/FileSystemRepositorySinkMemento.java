package ada.domain.dvc.values.repository.fs;

import ada.domain.dvc.values.repository.version.VersionDetailsMemento;
import ada.domain.dvc.values.repository.RepositorySinkMemento;
import ada.domain.legacy.repository.fs.FileSystemRepositorySettingsMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileSystemRepositorySinkMemento implements RepositorySinkMemento {

    private final Path root;

    private final FileSystemRepositorySettingsMemento settings;

    private final VersionDetailsMemento details;

    @JsonCreator
    public static FileSystemRepositorySinkMemento apply(
        @JsonProperty("root") Path directory,
        @JsonProperty("settings") FileSystemRepositorySettingsMemento settings,
        @JsonProperty("details") VersionDetailsMemento details) {

        return new FileSystemRepositorySinkMemento(directory, settings, details);
    }

}
