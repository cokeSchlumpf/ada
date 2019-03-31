package ada.vcs.client.converters.internal.contexts;

import ada.vcs.client.converters.internal.api.Context;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileContext implements Context {

    private final String checksum;

    public static FileContext empty() {
        return FileContext.apply("");
    }

    public static FileContext apply(String checksum) {
        return new FileContext(checksum.toLowerCase());
    }

    public static FileContext apply(Path path) {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path must be an existing file.");
        }

        try {
            HashCode hash = Hashing
                .sha256()
                .newHasher()
                .putLong(path.toFile().lastModified())
                .putLong(Files.size(path))
                .hash();

            return FileContext.apply(hash.toString());
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        os.write(checksum.getBytes());
    }

}
