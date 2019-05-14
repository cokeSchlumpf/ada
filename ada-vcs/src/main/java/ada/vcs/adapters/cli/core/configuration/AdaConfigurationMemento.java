package ada.vcs.adapters.cli.core.configuration;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.core.endpoints.EndpointMemento;
import ada.vcs.domain.shared.repository.api.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdaConfigurationMemento {

    private final User user;

    private final ResourceName endpoint;

    private final ResourceName namespace;

    private final List<EndpointMemento> endpoints;

    @JsonCreator
    public static AdaConfigurationMemento apply(
        @JsonProperty("user") User user,
        @JsonProperty("endpoint") ResourceName endpoint,
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("endpoints") List<EndpointMemento> endpoints) {

        return new AdaConfigurationMemento(user, endpoint, namespace, endpoints);
    }

}
