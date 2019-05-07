package ada.vcs.client.core.repository.fs;

import ada.vcs.client.core.repository.api.version.VersionDetailsMemento;
import ada.vcs.client.core.repository.api.RepositorySinkMemento;
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
