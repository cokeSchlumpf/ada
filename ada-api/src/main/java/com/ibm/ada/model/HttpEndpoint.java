package com.ibm.ada.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
@JsonSerialize(using = HttpEndpoint.Serializer.class)
@JsonDeserialize(using = HttpEndpoint.Deserializer.class)
public final class HttpEndpoint {

    private final URL baseUrl;

    public URI uri() {
        try {
            return baseUrl.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public URL url() {
        return baseUrl;
    }

    public HttpEndpoint resolve(String uri) {
        try {
            return apply(new URL(baseUrl + uri));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("%s is not a valid URI", uri));
        }
    }

    public static class Serializer extends StdSerializer<HttpEndpoint> {

        private Serializer() {
            super(HttpEndpoint.class);
        }

        @Override
        public void serialize(HttpEndpoint value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.baseUrl.toString());
        }

    }

    public static class Deserializer extends StdDeserializer<HttpEndpoint> {

        private Deserializer() {
            super(RelativePath.class);
        }

        @Override
        public HttpEndpoint deserialize(JsonParser p, DeserializationContext ignore) throws IOException {
            return HttpEndpoint.apply(new URL(p.getValueAsString()));
        }

    }

}
