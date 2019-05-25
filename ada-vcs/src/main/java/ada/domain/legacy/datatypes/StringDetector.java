package ada.domain.legacy.datatypes;

import ada.commons.util.Either;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.function.Function;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class StringDetector implements DataTypeDetector<StringDetector> {

    private boolean isOptional;

    private Proximity proximity;

    public static StringDetector apply() {
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
        } else {
            proximity.countCorrectValue();
        }
    }

    @Override
    public StringDetector withOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }

    @Override
    public Field type(String fieldName) {
        StringTypeHelper h = StringTypeHelper.apply(fieldName, isOptional);
        return DetectedField.apply(fieldName, "String", h, h);
    }

    private static Either<Object, Exception> parse(String value) {
        return Either.left(value);
    }

    private static boolean isNullValue(String value) {
        return value == null || value.trim().length() == 0;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class StringTypeHelper implements DetectedField.Builder, DetectedField.Parser {

        private final String fieldName;

        private final boolean isOptional;

        @Override
        public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build() {
            if (isOptional) {
                return builder -> builder.optionalString(fieldName);
            } else {
                return builder -> builder.requiredString(fieldName);
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
                return StringDetector.parse(value);
            }
        }

    }

}
