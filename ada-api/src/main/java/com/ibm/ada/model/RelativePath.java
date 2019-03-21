package com.ibm.ada.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

/**
 * A wrapper object around {@link java.nio.file.Path} which ensures that the path is relative.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize(using = RelativePath.Serializer.class)
@JsonDeserialize(using = RelativePath.Deserializer.class)
public final class RelativePath implements Path {

    private final Path path;

    public static RelativePath apply(Path path, Path relativeTo) {
        if (path.isAbsolute()) {
            throw InvalidPathException.apply(path);
        }

        relativeTo = relativeTo.toAbsolutePath().normalize();
        Path pathAbsolute = relativeTo.resolve(path).normalize();

        if (!pathAbsolute.startsWith(relativeTo)) {
            throw InvalidPathException.apply(path, relativeTo);
        }
        return new RelativePath(path);
    }

    public static RelativePath apply(Path path) {
        if (path.isAbsolute()) {
            throw InvalidPathException.apply(path);
        }

        return new RelativePath(path);
    }

    public static RelativePath apply(String path) {
        return apply(Paths.get(path));
    }

    @Override
    public FileSystem getFileSystem() {
        return path.getFileSystem();
    }

    @Override
    public boolean isAbsolute() {
        return path.isAbsolute();
    }

    @Override
    public Path getRoot() {
        return path.getRoot();
    }

    @Override
    public Path getFileName() {
        return path.getFileName();
    }

    @Override
    public RelativePath getParent() {
        return RelativePath.apply(path.getParent());
    }

    @Override
    public int getNameCount() {
        return path.getNameCount();
    }

    @Override
    public Path getName(int index) {
        return path.getName(index);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return path.subpath(beginIndex, endIndex);
    }

    @Override
    public boolean startsWith(Path other) {
        return path.startsWith(other);
    }

    @Override
    public boolean startsWith(String other) {
        return path.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return path.endsWith(other);
    }

    @Override
    public boolean endsWith(String other) {
        return path.endsWith(other);
    }

    @Override
    public Path normalize() {
        return path.normalize();
    }

    @Override
    public RelativePath resolve(Path other) {
        return RelativePath.apply(path.resolve(other));
    }

    @Override
    public RelativePath resolve(String other) {
        return RelativePath.apply(path.resolve(other));
    }

    @Override
    public Path resolveSibling(Path other) {
        return path.resolveSibling(other);
    }

    @Override
    public Path resolveSibling(String other) {
        return path.resolveSibling(other);
    }

    @Override
    public Path relativize(Path other) {
        return path.relativize(other);
    }

    @Override
    public URI toUri() {
        return path.toUri();
    }

    @Override
    public Path toAbsolutePath() {
        return path.toAbsolutePath();
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return path.toRealPath(options);
    }

    @Override
    public File toFile() {
        return path.toFile();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return path.register(watcher, events, modifiers);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return path.register(watcher, events);
    }

    @Override
    public Iterator<Path> iterator() {
        return path.iterator();
    }

    @Override
    public int compareTo(Path other) {
        return path.compareTo(other);
    }

    public static class Serializer extends StdSerializer<RelativePath> {

        private Serializer() {
            super(RelativePath.class);
        }

        @Override
        public void serialize(RelativePath value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.path.toString());
        }

    }

    public static class Deserializer extends StdDeserializer<RelativePath> {

        private Deserializer() {
            super(RelativePath.class);
        }

        @Override
        public RelativePath deserialize(JsonParser p, DeserializationContext ignore) throws IOException {
            return RelativePath.apply(Paths.get(p.readValueAs(String.class)));
        }

    }

    public static class InvalidPathException extends RuntimeException {

        private InvalidPathException(String message) {
            super(message);
        }

        public static InvalidPathException apply(Path path) {
            String message = String.format("Provided path '%s' is not relative.", path);
            return new InvalidPathException(message);
        }

        public static InvalidPathException apply(Path path, Path childrenOf) {
            String message = String.format(
                "Provided path '%s' must be children of '%s'.",
                path.toAbsolutePath(),
                childrenOf.toAbsolutePath());

            return new InvalidPathException(message);
        }

    }

}
