package ada.vcs.domain.legacy.datatypes;

import ada.commons.util.Either;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply")
public final class DetectedField implements Field {

    private final String fieldName;

    private final String typeName;

    private final Builder builder;

    private final Parser parser;

    private final String description;

    public static DetectedField apply(String name, String type, Builder builder, Parser parser) {
        return apply(name, type, builder, parser, null);
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> builder() {
        return builder.build();
    }

    @Override
    public Either<Object, Exception> parse(String value) {
        try {
            return parser
                .parse(value)
                .mapRight(e -> NoValidValueException.apply(getFieldName(), value, e));
        } catch (Exception e) {
            return Either.right(NoValidValueException.apply(getFieldName(), value));
        }
    }

    @FunctionalInterface
    interface Builder {

        Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build();

    }

    @FunctionalInterface
    interface Parser {

        Either<Object, Exception> parse(String value);

    }

    public static class NotOptionalException extends Exception {

        private NotOptionalException(String message) {
            super(message);
        }

        public static NotOptionalException apply(String fieldName) {
            String message = String.format("The field '%s' is not optional", fieldName);
            return new NotOptionalException(message);
        }

    }

    public static class NoValidValueException extends Exception {

        private NoValidValueException(String message, Throwable cause) {
            super(message, cause);
        }

        public static Exception apply(String fieldName, String value, Exception cause) {
            String message = String.format("The value '%s' is not valid for field '%s'", value, fieldName);

            if (cause instanceof NotOptionalException) {
                return cause;
            } else {
                return new NoValidValueException(message, cause);
            }
        }

        public static Exception apply(String fieldName, String value) {
            return apply(fieldName, value, null);
        }

    }

}
