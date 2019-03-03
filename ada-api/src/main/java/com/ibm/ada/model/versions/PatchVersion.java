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
@JsonSerialize(using = PatchVersion.Serializer.class)
@JsonDeserialize(using = PatchVersion.Deserializer.class)
public class PatchVersion {

    private static final String LATEST_POSTFIX = "-snapshot";

    private final int major;

    private final int minor;

    private final int patch;

    private boolean committed;

    /**
     * Creates a PatchVersion based on a string.
     *
     * @param versionString The string representation of the version.
     * @return The parsed version
     * @throws InvalidVersionStringException If string cannot be parsed as version.
     */
    public static PatchVersion apply(String versionString) {
        try {
            String[] parts = versionString.split("[.]");

            int major = Integer.valueOf(parts[0]);
            int minor = Integer.valueOf(parts[1]);
            int patch = parts.length > 2 ? Integer.valueOf(parts[2].split("-")[0]) : 0;

            boolean committed = !versionString.endsWith(LATEST_POSTFIX);

            return PatchVersion.apply(major, minor, patch, committed);
        } catch (Exception e) {
            throw InvalidVersionStringException.apply(versionString);
        }
    }

    public Version version() {
        return Version.apply(major, minor);
    }

    /**
     * Returns a string representation of the string.
     *
     * @return a string representation.
     */
    public String versionString() {
        return String.format("%d.%d.%d%s", major, minor, patch, committed ? "" : LATEST_POSTFIX);
    }

    public static class Serializer extends StdSerializer<PatchVersion> {

        private Serializer() {
            super(PatchVersion.class);
        }

        @Override
        public void serialize(PatchVersion value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.versionString());
        }

    }

    public static class Deserializer extends StdDeserializer<PatchVersion> {

        private Deserializer() {
            super(Version.class);
        }

        @Override
        public PatchVersion deserialize(JsonParser p, DeserializationContext ignored) throws IOException {
            return PatchVersion.apply(p.readValueAs(String.class));
        }

    }

}
