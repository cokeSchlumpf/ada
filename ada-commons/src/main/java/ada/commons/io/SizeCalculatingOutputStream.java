package ada.commons.io;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class SizeCalculatingOutputStream extends OutputStream {

    private boolean closed;

    private long bytes;

    private final OutputStream os;

    private final ReadWriteLock readWriteLock;

    public static SizeCalculatingOutputStream apply(OutputStream os) {
        return apply(false, 0, os, new ReentrantReadWriteLock());
    }

    public static SizeCalculatingOutputStream apply(OutputStream os, long initialBytes) {
        return apply(false, initialBytes, os, new ReentrantReadWriteLock());
    }

    public static SizeCalculatingOutputStream apply(Path path) throws IOException {
        if (Files.exists(path) && !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path must be a regular file. Not a directory.");
        }

        if (Files.exists(path)) {
            long bytes = Files.size(path);
            FileOutputStream fos = new FileOutputStream(path.toFile(), true);
            return apply(false, bytes, fos, new ReentrantReadWriteLock());

        } else {
            Path parent = path.getParent();

            if (parent == null) {
                throw InvalidPathException.apply(path);
            }

            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Files.createFile(path);
            FileOutputStream fos = new FileOutputStream(path.toFile(), false);
            return apply(false, 0, fos, new ReentrantReadWriteLock());
        }
    }

    private void checkClosed() throws IOException {
        if (closed) {
            throw StreamAlreadyClosedException.apply();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.os.close();
        this.closed = true;
    }

    @Override
    public void write(int b) throws IOException {
        checkClosed();

        Lock lock = readWriteLock.writeLock();
        lock.lock();

        try {
            os.write(b);
            bytes = bytes + 1;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();

        Lock lock = readWriteLock.writeLock();
        lock.lock();

        try {
            os.write(b, off, len);
            bytes = bytes + len;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        checkClosed();

        Lock lock = readWriteLock.writeLock();
        lock.lock();

        try {
            os.write(b);
            bytes = bytes + b.length;
        } finally {
            lock.unlock();
        }
    }

    public long getFileSize() {
        return bytes;
    }

    public static class StreamAlreadyClosedException extends IOException {

        private StreamAlreadyClosedException() {
            super("The stream is already closed.");
        }

        public static StreamAlreadyClosedException apply() {
            return new StreamAlreadyClosedException();
        }

    }

}
