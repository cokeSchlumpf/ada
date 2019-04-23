package ada.vcs.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Writable {

    void writeTo(OutputStream os) throws IOException;

    default void writeTo(Path path) throws IOException {
        try(OutputStream os = Files.newOutputStream(path)) {
            writeTo(os);
        }
    }

}
