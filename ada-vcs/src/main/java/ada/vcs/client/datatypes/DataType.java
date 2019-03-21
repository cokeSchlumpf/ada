package ada.vcs.client.datatypes;

import ada.commons.Either;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.Optional;
import java.util.function.Function;

public interface DataType {

    String name();

    Optional<String> description();

    Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> builder(String fieldName);

    Either<Object, Exception> parse(String value);

}
