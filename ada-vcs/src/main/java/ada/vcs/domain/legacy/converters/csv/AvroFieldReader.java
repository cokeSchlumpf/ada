package ada.vcs.domain.legacy.converters.csv;

import ada.commons.util.Either;
import ada.vcs.domain.legacy.datatypes.DataTypeDetector;
import ada.vcs.domain.legacy.datatypes.DataTypeMatcher;
import ada.vcs.domain.legacy.datatypes.Field;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AvroFieldReader {

    private final Field parser;

    public static AvroFieldReader apply(String fieldName, Schema.Field field, DataTypeMatcher matcher) {
        return new AvroFieldReader(detector(matcher, field.schema()).type(fieldName));
    }

    private static DataTypeDetector<?> detector(DataTypeMatcher matcher, Schema schema) {
        switch (schema.getType().getName().toLowerCase()) {
            case "boolean":
                return matcher
                    .getBoolean()
                    .withOptional(false);
            case "double":
                return matcher
                    .getDouble()
                    .withOptional(false);
            case "enum":
                return matcher
                    .getEnum()
                    .withSymbols(schema.getEnumSymbols())
                    .withOptional(false);
            case "int":
                return matcher
                    .getInteger()
                    .withOptional(false);
            case "string":
                return matcher
                    .getString()
                    .withOptional(false);
            case "union":
                List<Schema> subTypes = schema.getTypes();
                if (subTypes.size() == 2 && subTypes.get(0).getType().getName().equals("null")) {
                    return detector(matcher, subTypes.get(1))
                        .withOptional(true);
                } else {
                    throw new IllegalArgumentException("Cannot handle UNIONS other than [ \"null\", \"<TYPE>\" ]");
                }
            default:
                throw new IllegalArgumentException("Cannot handle Avro Type: " + schema.getType());
        }
    }

    public Either<Object, Exception> read(String value) {
        return parser.parse(value);
    }

}
