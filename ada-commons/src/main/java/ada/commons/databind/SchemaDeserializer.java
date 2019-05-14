package ada.commons.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.avro.Schema;

import java.io.IOException;

public final class SchemaDeserializer extends StdDeserializer<Schema> {

    public SchemaDeserializer() {
        super(Schema.class);
    }


    @Override
    public Schema deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new Schema.Parser().setValidate(true).parse(p.getCodec().readTree(p).toString());
    }

}
