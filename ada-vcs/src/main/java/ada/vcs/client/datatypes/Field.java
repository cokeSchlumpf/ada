package ada.vcs.client.datatypes;

import ada.commons.util.Either;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.Optional;
import java.util.function.Function;

public interface Field {

    String getFieldName();

    String getTypeName();

    Optional<String> description();

    Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> builder();

    Either<Object, Exception> parse(String value);

}
