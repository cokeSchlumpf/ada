package ada.domain.dvc.values.repository.fs;

import ada.domain.dvc.values.repository.RefSpec;
import ada.domain.dvc.values.repository.RepositorySourceMemento;
import ada.domain.legacy.repository.fs.FileSystemRepositorySettingsMemento;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileSystemRepositorySourceMemento implements RepositorySourceMemento {

    private final Path root;

    private final FileSystemRepositorySettingsMemento settings;

    private final RefSpec.VersionRef version;

    public static FileSystemRepositorySourceMemento apply(
        @JsonProperty("root") Path root,
        @JsonProperty("settings") FileSystemRepositorySettingsMemento settings,
        @JsonProperty("version")RefSpec.VersionRef version) {

        return new FileSystemRepositorySourceMemento(root, settings, version);
    }

}
