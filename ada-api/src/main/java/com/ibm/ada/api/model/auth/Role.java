package com.ibm.ada.api.model.auth;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;

@Value
@AllArgsConstructor(staticName = "apply")
public class Role {

    String name;

    public static class Serializer extends StdSerializer<Role> {

        private Serializer() {
            super(Role.class);
        }

        @Override
        public void serialize(Role value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getName());
        }

    }

    public static class Deserializer extends StdDeserializer<Role> {

        private Deserializer() {
            super(Role.class);
        }

        @Override
        public Role deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Role.apply(p.getValueAsString());
        }

    }

}
