package ada.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class FileSystemRemoteMemento implements RemoteMemento {

    private final ResourceName alias;

    private final Path dir;

    @JsonCreator
    public static FileSystemRemoteMemento apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("dir") Path dir) {

        return new FileSystemRemoteMemento(alias, dir);
    }

}
