package ada.vcs.client.core.endpoints;

import ada.commons.util.ResourceName;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethodMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URL;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EndpointMemento {

    private final ResourceName alias;

    private final URL url;

    private final AuthenticationMethodMemento authentication;

    @JsonCreator
    public static EndpointMemento apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("url") URL url,
        @JsonProperty("authentication") AuthenticationMethodMemento authentication) {

        return new EndpointMemento(alias, url, authentication);
    }

}
