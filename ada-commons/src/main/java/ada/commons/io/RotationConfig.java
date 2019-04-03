package ada.commons.io;

import ada.commons.util.FileSize;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Value
@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public class RotationConfig {

    private static final FilenameTemplate FILENAME_TEMPLATE_DEFAULT = FilenameTemplate.apply("records", "avro");
    private static final FileSize FILE_SIZE_DEFAULT = FileSize.apply(500, FileSize.Unit.MEGABYTES);
    private static final FileSize CHUNK_SIZE_DEFAULT = FileSize.apply(1, FileSize.Unit.MEGABYTES);
    private static final FileSize CHUNK_SIZE_MIN = FileSize.apply(1, FileSize.Unit.KILOBYTES);

    private final AtomicInteger uniqueFilenameCounter;
    private final Path directory;
    private final FilenameTemplate filenameTemplate;
    private final FileSize maxFileSize;
    private final FileSize maxChunkSize;
    private final NumberFormat filenameCounterFormat;
    private final DateFormat filenameDateFormat;

    public static RotationConfig apply(Path directory) {
        return apply(directory, FILENAME_TEMPLATE_DEFAULT);
    }

    public static RotationConfig apply(Path directory, FilenameTemplate filenameTemplate) {
        return apply(directory, filenameTemplate, FILE_SIZE_DEFAULT);
    }

    public static RotationConfig apply(Path directory, FilenameTemplate filenameTemplate, FileSize fileSize) {
        return apply(directory, filenameTemplate, fileSize, CHUNK_SIZE_DEFAULT);
    }

    public static RotationConfig apply(Path directory, FilenameTemplate filenameTemplate, FileSize fileSize, FileSize chunkSize) {

        // check chunk size settings
        if (chunkSize.getBytes() < CHUNK_SIZE_MIN.getBytes()) {
            throw InvalidChunkSizeException.tooSmall(chunkSize, CHUNK_SIZE_MIN);
        }

        if (chunkSize.getBytes() > fileSize.getBytes()) {
            throw InvalidChunkSizeException.tooLarge(chunkSize, fileSize);
        }

        // create config
        return apply(
            new AtomicInteger(1),
            directory,
            filenameTemplate,
            fileSize,
            chunkSize,
            new DecimalFormat("####0"),
            new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS"));
    }

    public String createFilename() {
        return String.format(
            filenameTemplate.getTemplate(),
            filenameDateFormat.format(new Date()),
            filenameCounterFormat.format(uniqueFilenameCounter.getAndIncrement()));
    }

    public Path getNextFile(boolean reuseExistingFile) {
        String filename = createFilename();
        Path next = getDirectory().resolve(filename);

        if (reuseExistingFile) {
            final List<Path> files = Lists.newArrayList();

            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(getDirectory())) {
                for (Path path : directoryStream) {
                    files.add(path);
                }
            } catch (IOException ignored) { }

            return files
                .stream()
                .filter(p -> {
                    String n = p.getFileName().toString();

                    return n.startsWith(getFilenameTemplate().getPrefix()) &&
                           n.endsWith(getFilenameTemplate().getExtension()) &&
                           n.length() == filename.length();
                })
                .filter(p -> {
                    try {
                        return Files.size(p) < getMaxFileSize().getBytes();
                    } catch (IOException e) {
                        return false;
                    }
                })
                .min(Comparator.reverseOrder())
                .orElse(next);
        } else {
            return next;
        }
    }

}
