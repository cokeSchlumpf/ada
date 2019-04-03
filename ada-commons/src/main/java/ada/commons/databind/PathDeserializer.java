package ada.commons.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathDeserializer extends StdDeserializer<Path> {

    public PathDeserializer() {
        super(Path.class);
    }


    @Override
    public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return Paths.get(p.readValueAs(String.class));
    }

}