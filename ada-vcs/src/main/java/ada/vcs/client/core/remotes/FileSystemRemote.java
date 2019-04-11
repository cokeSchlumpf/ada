package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.FileSystemDependent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.file.Files;
import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileSystemRemote implements Remote, FileSystemDependent<FileSystemRemote> {

    private final ResourceName alias;

    private final Path dir;

    @JsonCreator
    public static FileSystemRemote apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("dir") Path dir) {

        if (Files.exists(dir) && !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path must be a directory, not a file");
        }

        return new FileSystemRemote(alias, dir);
    }

    @Override
    public FileSystemRemote resolve(Path to) {
        return apply(alias, to.resolve(dir));
    }

    @Override
    public FileSystemRemote relativize(Path to) {
        return apply(alias, to.relativize(dir));
    }

    @Override
    public String getInfo() {
        return dir.toString();
    }
}
