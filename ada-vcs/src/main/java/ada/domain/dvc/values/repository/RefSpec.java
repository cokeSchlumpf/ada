package ada.domain.dvc.values.repository;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RefSpec {

    private RefSpec() {

    }

    public static VersionRef fromId(String id) {
        return VersionRef.apply(id);
    }

    public static VersionRef fromFile(Path path) {
        return Operators.suppressExceptions(() -> {
            HashCode hash = Hashing
                .goodFastHash(128)
                .newHasher()
                .putLong(path.toFile().lastModified())
                .putLong(Files.size(path))
                .hash();

            return RefSpec.VersionRef.apply(hash.toString());
        });
    }

    public static TagRef fromTag(String tag) {
        return TagRef.apply(ResourceName.apply(tag));
    }

    public static TagRef fromTag(ResourceName tag) {
        return TagRef.apply(tag);
    }

    public static RefSpec fromString(String s) {
        Pattern pattern = Pattern.compile("([^/]+)/([\\w-]+)");
        Matcher matcher = pattern.matcher(s);

        if (!matcher.matches() && !matcher.group(1).equals("tags") && !matcher.group(1).equals("versions")) {
            throw new IllegalArgumentException("The provided string is not a valid RefSpec.");
        } else if (matcher.group(1).equals("tags")) {
            return TagRef.apply(ResourceName.apply(matcher.group(2)));
        } else {
            return VersionRef.apply(matcher.group(2));
        }
    }

    public abstract <T> T map(
        Operators.ExceptionalFunction<TagRef, T> mapTagRef,
        Operators.ExceptionalFunction<VersionRef, T> mapPushRef);

    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor(staticName = "apply")
    public static final class TagRef extends RefSpec {

        private final ResourceName alias;

        @Override
        public <T> T map(Operators.ExceptionalFunction<TagRef, T> mapTagRef, Operators.ExceptionalFunction<VersionRef, T> mapPushRef) {
            return Operators.suppressExceptions(() -> mapTagRef.apply(this));
        }

        public ResourceName getAlias() {
            return alias;
        }

        @Override
        public String toString() {
            return String.format("tags/%s", alias.getValue());
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor(staticName = "apply")
    public static class VersionRef extends RefSpec {

        private final String id;

        @Override
        public <T> T map(Operators.ExceptionalFunction<TagRef, T> mapTagRef, Operators.ExceptionalFunction<VersionRef, T> mapPushRef) {
            return Operators.suppressExceptions(() -> mapPushRef.apply(this));
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return String.format("versions/%s", id);
        }

    }
}
