package ada.vcs.domain.shared.datatypes;

import ada.commons.util.Either;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.function.Function;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class IntegerDetector implements DataTypeDetector<IntegerDetector> {

    private boolean isOptional;

    private Proximity proximity;

    public static IntegerDetector apply() {
        return apply(false, Proximity.apply());
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

        parse(value).apply(
            i -> proximity.countCorrectValue(),
            i -> proximity.countInvalidValue());
    }

    @Override
    public IntegerDetector withOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }

    @Override
    public Field type(String fieldName) {
        IntegerTypeHelper h = IntegerTypeHelper.apply(fieldName, isOptional);
        return DetectedField.apply(fieldName, "Integer", h, h);
    }

    private static Either<Object, Exception> parse(String value) {
        return Either.result(() -> Integer.valueOf(value));
    }

    private static boolean isNullValue(String value) {
        return value == null || value.trim().length() == 0;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class IntegerTypeHelper implements DetectedField.Builder, DetectedField.Parser {

        private final String fieldName;

        private final boolean isOptional;

        @Override
        public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build() {
            if (isOptional) {
                return builder -> builder.optionalInt(fieldName);
            } else {
                return builder -> builder.requiredInt(fieldName);
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
                return IntegerDetector.parse(value);
            }
        }

    }

}
