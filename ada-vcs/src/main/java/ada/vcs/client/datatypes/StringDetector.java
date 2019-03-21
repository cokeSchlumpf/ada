package ada.vcs.client.datatypes;

import ada.commons.Either;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.function.Function;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class StringDetector implements DataTypeDetector {

    private boolean isOptional;

    private Proximity proximity;

    public static StringDetector apply() {
        return apply(false, Proximity.apply());
    }

    @Override
    public Proximity proximity() {
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
    public DataType type() {
        StringTypeHelper h = StringTypeHelper.apply(isOptional);
        return DetectedDataType.apply("String", h, h);
    }

    private static Either<Object, Exception> parse(String value) {
        if (isNullValue(value)) {
            return Either.left(null);
        } else {
            return Either.left(value);
        }
    }

    private static boolean isNullValue(String value) {
        return value == null || value.trim().length() == 0;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class StringTypeHelper implements DetectedDataType.Builder, DetectedDataType.Parser {

        private final boolean isOptional;

        @Override
        public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build(String fieldName) {
            if (isOptional) {
                return builder -> builder.optionalString(fieldName);
            } else {
                return builder -> builder.requiredString(fieldName);
            }
        }

        @Override
        public Either<Object, Exception> parse(String value) {
            return StringDetector.parse(value);
        }

    }

}
