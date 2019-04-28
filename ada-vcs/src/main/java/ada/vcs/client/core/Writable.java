package ada.vcs.client.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Writable {

    void writeTo(OutputStream os) throws IOException;

    default void writeTo(Path path) throws IOException {
        try(OutputStream os = Files.newOutputStream(path)) {
            writeTo(os);
        }
    }

    default String writeToString() throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            writeTo(baos);
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

}
