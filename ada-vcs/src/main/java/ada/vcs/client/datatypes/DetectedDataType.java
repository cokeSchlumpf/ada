package ada.vcs.client.datatypes;

import ada.commons.Either;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply")
public final class DetectedDataType implements DataType {

    private final String name;

    private final Builder builder;

    private final Parser parser;

    private final String description;

    public static DetectedDataType apply(String name, Builder builder, Parser parser) {
        return apply(name, builder, parser, null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> builder(String fieldName) {
        return builder.build(fieldName);
    }

    @Override
    public Either<Object, Exception> parse(String value) {
        try {
            return parser.parse(value);
        } catch (Exception e) {
            return Either.Right.apply(e);
        }
    }

    @FunctionalInterface
    interface Builder {

        Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build(String fieldName);

    }

    @FunctionalInterface
    interface Parser {

        Either<Object, Exception> parse(String value);

    }

}
