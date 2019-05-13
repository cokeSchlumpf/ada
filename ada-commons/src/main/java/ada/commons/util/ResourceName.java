package ada.commons.util;

import ada.commons.exceptions.InvalidResourceNameException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize(using = ResourceName.Serializer.class)
@JsonDeserialize(using = ResourceName.Deserializer.class)
public class ResourceName {

    private final String value;

    public static ResourceName apply(String value) {
        if (value == null || value.length() == 0) {
            throw InvalidResourceNameException.apply(value);
        }

        String name = NameFactory
            .apply(NameFactory.Defaults.LOWERCASE_HYPHENATE)
            .create(value);

        if (name.length() == 0) {
            throw InvalidResourceNameException.apply(value);
        }

        return new ResourceName(name);
    }

    @Override
    public String toString() {
        return value;
    }

    public static class Serializer extends StdSerializer<ResourceName> {

        private Serializer() {
            super(ResourceName.class);
        }

        @Override
        public void serialize(ResourceName value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getValue());
        }

    }

    public static class Deserializer extends StdDeserializer<ResourceName> {

        private Deserializer() {
            super(ResourceName.class);
        }

        @Override
        public ResourceName deserialize(JsonParser p, DeserializationContext ignore) throws IOException {
            return ResourceName.apply(p.readValueAs(String.class));
        }

    }

}
