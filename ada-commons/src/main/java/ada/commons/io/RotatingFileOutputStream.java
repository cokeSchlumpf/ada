package ada.commons.io;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RotatingFileOutputStream extends OutputStream {

    private boolean closed;
    private final RotationConfig config;
    private SizeCalculatingOutputStream os;

    public static RotatingFileOutputStream apply(RotationConfig config) throws IOException {

        checkDirectory(config.getDirectory());
        Path file = config.getNextFile(true);

        return new RotatingFileOutputStream(false, config, SizeCalculatingOutputStream.apply(file));
    }

    private static void checkDirectory(Path directory) {
        if (Files.exists(directory) && !Files.isDirectory(directory)) {
            throw InvalidDirectoryPathException.apply(directory);
        }

        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw DirectoryCreationException.apply(directory, e);
            }
        }
    }


    private void checkClosed() throws IOException {
        if (closed) {
            throw StreamAlreadyClosedException.apply();
        }
    }

    private void rotate() throws IOException {
        Lock lock = new ReentrantReadWriteLock().writeLock();

        try {
            lock.lock();

            if (os.getFileSize() >= config.getMaxFileSize().getBytes()) {
                Path next = this.config.getNextFile(false);

                os.close();
                os = SizeCalculatingOutputStream.apply(next);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(int b) throws IOException {
        checkClosed();
        rotate();
        os.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        checkClosed();
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();

        if (len > config.getMaxChunkSize().getBytes()) {
            int chunkSize = Long.valueOf(config.getMaxChunkSize().getBytes()).intValue();
            int partsCount = len / chunkSize;

            for (int i = 0; i < partsCount; i++) {
                write(b, off + i * chunkSize, i == chunkSize - 1 ? len % chunkSize : chunkSize);
            }
        } else {
            rotate();
            os.write(b, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.os.close();
        this.closed = true;
    }

    public static class InvalidDirectoryPathException extends IllegalArgumentException {

        private String path;

        private InvalidDirectoryPathException(String message, String path) {
            super(message);
            this.path = path;
        }

        public static InvalidDirectoryPathException apply(Path path) {
            String pathAsString = path.toAbsolutePath().toString();
            String message = String.format("The provided path '%s' is no directory.", pathAsString);
            return new InvalidDirectoryPathException(message, pathAsString);
        }

        public String getPath() {
            return path;
        }

    }

    public static class DirectoryCreationException extends IllegalArgumentException {

        private String path;

        private DirectoryCreationException(String message, String path, IOException cause) {
            super(message, cause);
            this.path = path;
        }

        public static DirectoryCreationException apply(Path path, IOException cause) {
            String pathAsString = path.toAbsolutePath().toString();
            String message = String.format("The directory for the provided path '%s' cannot be created.", pathAsString);
            return new DirectoryCreationException(message, pathAsString, cause);
        }

        public String getPath() {
            return path;
        }

    }

    public static class StreamAlreadyClosedException extends IOException {

        private StreamAlreadyClosedException() {
            super("The stream has been already closed");
        }

        public static StreamAlreadyClosedException apply() {
            return new StreamAlreadyClosedException();
        }

    }

}
