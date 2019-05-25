package ada.domain.legacy.datatypes;

import ada.commons.util.Either;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class BooleanDetector implements DataTypeDetector<BooleanDetector> {

    private final List<Pair<String, String>> formats;

    private Pair<String, String> defaultFormat;

    private final Proximity proximity;

    private boolean isOptional;

    private Pair<String, String> format;

    public static BooleanDetector apply(List<Pair<String, String>> formats, Pair<String, String> defaultFormat) {
        return apply(ImmutableList.copyOf(formats), defaultFormat, Proximity.apply(), false, null);
    }

    public static BooleanDetector apply() {
        List<Pair<String, String>> formats = Lists.newArrayList(
            Pair.of("true", "false"),
            Pair.of("t", "f"),
            Pair.of("yes", "no"),
            Pair.of("1", "0"),
            Pair.of("w", "f"));

        return apply(formats, formats.get(0));
    }

    @Override
    public Proximity getProximity() {
        return proximity;
    }

    @Override
    public void hint(String value) {
        if (isNullValue(value)) {
            isOptional = true;
            proximity.countNullValue();
            return;
        }

        if (format == null) {
            format = formats
                .stream()
                .filter(format -> parse(value, format).map(i -> true, i -> false))
                .findFirst()
                .orElse(null);
        }

        if (format == null) {
            proximity.countInvalidValue();
        } else {
            parse(value, format).apply(
                i -> proximity.countCorrectValue(),
                i -> proximity.countInvalidValue());
        }
    }

    @Override
    public BooleanDetector withOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }

    @Override
    public Field type(String name) {
        BooleanDataTypeHelper h = BooleanDataTypeHelper.apply(
            name,
            isOptional,
            Optional.ofNullable(format).orElse(defaultFormat));

        return DetectedField.apply(name, "Boolean", h, h);
    }

    private static Either<Object, Exception> parse(String value, Pair<String, String> format) {
        if (isNullValue(value)) {
            return Either.left(null);
        } else {
            value = value.toLowerCase();

            if (value.equals(format.getLeft())) {
                return Either.left(Boolean.TRUE);
            } else if (value.equals(format.getRight())) {
                return Either.left(Boolean.FALSE);
            } else {
                String message = String.format("'%s' is not a valid boolean value", value);
                return Either.right(new IllegalArgumentException(message));
            }
        }
    }

    private static boolean isNullValue(String value) {
        return value == null || value.trim().length() == 0;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class BooleanDataTypeHelper implements DetectedField.Builder, DetectedField.Parser {

        private String fieldName;

        private boolean isOptional;

        private Pair<String, String> format;

        @Override
        public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build() {
            if (isOptional) {
                return builder -> builder.optionalBoolean(fieldName);
            } else {
                return builder -> builder.requiredBoolean(fieldName);
            }
        }

        @Override
        public Either<Object, Exception> parse(String value) {
            boolean isNull = isNullValue(value);

            if (isNull && isOptional) {
                return Either.left(null);
            } else if (isNull) {
                return Either.right(DetectedField.NotOptionalException.apply(fieldName));
            } else {
                return BooleanDetector.parse(value, format);
            }
        }

    }

}
