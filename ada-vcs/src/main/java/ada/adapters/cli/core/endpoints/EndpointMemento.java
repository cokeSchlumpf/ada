package ada.adapters.cli.core.endpoints;

import ada.adapters.client.modifiers.AuthenticationMethodMemento;
import ada.commons.util.ResourceName;
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
