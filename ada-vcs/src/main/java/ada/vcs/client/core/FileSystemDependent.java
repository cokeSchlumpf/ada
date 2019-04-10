package ada.vcs.client.core;

import java.nio.file.Path;

public interface FileSystemDependent<T extends FileSystemDependent<T>> {

    T resolve(Path to);

    T relativize(Path to);

}
