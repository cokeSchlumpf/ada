package ada.commons.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.avro.Schema;

import java.io.IOException;

public class SchemaSerializer extends StdSerializer<Schema> {

    private final ObjectMapper om;

    public SchemaSerializer() {
        super(Schema.class);
        this.om = new ObjectMapper();
    }

    @Override
    public void serialize(Schema value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        JsonNode node = om.readValue(value.toString(false), JsonNode.class);
        jgen.writeObject(node);
    }

}
