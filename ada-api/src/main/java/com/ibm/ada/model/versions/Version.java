package com.ibm.ada.model.versions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.ibm.ada.exceptions.InvalidVersionStringException;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;

@Value
@AllArgsConstructor(staticName = "apply")
@JsonSerialize(using = Version.Serializer.class)
@JsonDeserialize(using = Version.Deserializer.class)
public class Version {

    private final int major;

    private final int minor;

    /**
     * Creates a Version based on a string.
     *
     * @param versionString The string representation of the version.
     * @return The parsed version
     * @throws InvalidVersionStringException If string cannot be parsed as version.
     */
    public static Version apply(String versionString) {
        try {
            String[] parts = versionString.split("[.]");

            int major = Integer.valueOf(parts[0]);
            int minor = Integer.valueOf(parts[1]);

            return Version.apply(major, minor);
        } catch (Exception e) {
            throw InvalidVersionStringException.apply(versionString);
        }
    }

    /**
     * Returns a string representation of the string.
     *
     * @return a string representation.
     */
    public String versionString() {
        return String.format("%d.%d", major, minor);
    }

    public static class Serializer extends StdSerializer<Version> {

        private Serializer() {
            super(Version.class);
        }

        @Override
        public void serialize(Version value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.versionString());
        }

    }

    public static class Deserializer extends StdDeserializer<Version> {

        private Deserializer() {
            super(Version.class);
        }

        @Override
        public Version deserialize(JsonParser p, DeserializationContext ignored) throws IOException {
            return Version.apply(p.readValueAs(String.class));
        }

    }

}
