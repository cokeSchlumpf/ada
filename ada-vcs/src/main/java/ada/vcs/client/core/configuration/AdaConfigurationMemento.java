package ada.vcs.client.core.configuration;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.endpoints.EndpointMemento;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethodMemento;
import ada.vcs.shared.repository.api.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdaConfigurationMemento {

    private final User user;

    private final ResourceName endpoint;

    private final List<EndpointMemento> endpoints;

    @JsonCreator
    public static AdaConfigurationMemento apply(
        @JsonProperty("user") User user,
        @JsonProperty("endpoint") ResourceName endpoint,
        @JsonProperty("endpoints") List<EndpointMemento> endpoints) {

        return new AdaConfigurationMemento(user, endpoint, endpoints);
    }

}
