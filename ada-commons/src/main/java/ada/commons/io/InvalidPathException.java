package ada.commons.io;

import java.nio.file.Path;

public final class InvalidPathException extends RuntimeException {

    private final Path path;

    private InvalidPathException(String message, Path path) {
        super(message);
        this.path = path;
    }

    public static InvalidPathException apply(Path path) {
        String message = String.format("The provided path '%s' is not valid. Path must contain at least one element.", path.toAbsolutePath().toString());

        return new InvalidPathException(message, path);
    }

    public Path getPath() {
        return path;
    }
}
