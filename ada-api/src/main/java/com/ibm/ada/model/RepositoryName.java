package com.ibm.ada.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.ibm.ada.exceptions.InvalidResourceNameException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize(using = RepositoryName.Serializer.class)
@JsonDeserialize(using = RepositoryName.Deserializer.class)
public class RepositoryName {

    private final String value;

    public static RepositoryName apply(String value) {
        if (value == null || value.length() == 0) {
            throw InvalidResourceNameException.apply(value);
        }

        Pattern p1 = Pattern.compile("[a-zA-Z0-9]+");
        Matcher m = p1.matcher(value);

        String name = "";

        while (m.find()) {
            name = name.length() == 0 ? m.group() : name + "-" + m.group();
        }

        name = name.toLowerCase();

        if (name.length() == 0) {
            throw InvalidResourceNameException.apply(value);
        }

        return new RepositoryName(name);
    }

    public static class Serializer extends StdSerializer<RepositoryName> {

        private Serializer() {
            super(RepositoryName.class);
        }

        @Override
        public void serialize(RepositoryName value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getValue());
        }

    }

    public static class Deserializer extends StdDeserializer<RepositoryName> {

        private Deserializer() {
            super(RepositoryName.class);
        }

        @Override
        public RepositoryName deserialize(JsonParser p, DeserializationContext ignore) throws IOException {
            return RepositoryName.apply(p.readValueAs(String.class));
        }

    }

}
