package ada.vcs.server.adapters.client.modifiers;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class APIKeyAuthentication implements RequestModifier, AuthenticationMethod, AuthenticationMethodMemento {

    private static final String CLIENT_ID = "client-id";
    private static final String API_KEY = "api-key";

    @JsonProperty(CLIENT_ID)
    private final String clientId;

    @JsonProperty(API_KEY)
    private final String apiKey;

    @JsonCreator
    public static APIKeyAuthentication apply(
        @JsonProperty(CLIENT_ID) String clientId,
        @JsonProperty(API_KEY) String apiKey) {

        return new APIKeyAuthentication(clientId, apiKey);
    }

    @Override
    public Http modifyClient(Http http) {
        return http;
    }

    @Override
    public HttpRequest modifyRequest(HttpRequest request) {
        ArrayList<HttpHeader> headers = Lists.newArrayList(
            HttpHeader.parse("x-ibm-client-id", clientId),
            HttpHeader.parse("x-ibm-secret", apiKey));

        return request.withHeaders(headers);
    }

    @Override
    public String info() {
        return String.format("API authentication (client-id: %s)", clientId);
    }

    @Override
    public AuthenticationMethodMemento memento() {
        return this;
    }
}
