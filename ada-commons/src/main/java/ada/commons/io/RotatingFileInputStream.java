package ada.commons.io;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor(staticName = "apply")
public final class RotatingFileInputStream extends InputStream {

    private final List<Path> remaining;

    private InputStream fis;

    public static RotatingFileInputStream apply(List<Path> files) {
        return RotatingFileInputStream.apply(Lists.newArrayList(files), null);
    }

    public static RotatingFileInputStream apply(RotationConfig config) throws IOException {
        final List<Path> files = Lists.newArrayList();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(config.getDirectory())) {
            for (Path path : directoryStream) {
                Path file = path.getFileName();

                if (file != null && file.toString().endsWith(config.getFilenameTemplate().getExtension())) {
                    files.add(path);
                }
            }
        }

        files.sort(Comparator.naturalOrder());
        return apply(files);
    }

    @Override
    public synchronized int read() throws IOException {
        if (fis == null && !remaining.isEmpty()) {
            fis = new FileInputStream(remaining.get(0).toFile());
            remaining.remove(0);
            return read();
        } else if (fis == null) {
            return -1;
        } else {
            int next = fis.read();

            if (next == -1) {
                fis.close();
                fis = null;
                return read();
            } else {
                return next;
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (fis != null) {
            fis.close();
        }
    }
}
