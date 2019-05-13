package ada.vcs.client.core.endpoints;

import ada.commons.util.ResourceName;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethod;
import ada.vcs.server.adapters.client.repositories.RepositoriesClient;
import ada.vcs.server.adapters.client.repositories.RepositoriesClientFactory;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URL;
import java.util.Optional;

@Value
@Wither
@AllArgsConstructor(staticName = "apply")
public class Endpoint {

    private final RepositoriesClientFactory repositoriesClientFactory;

    private final ResourceName alias;

    private final URL url;

    private final AuthenticationMethod authenticationMethod;

    public EndpointMemento memento() {
        return EndpointMemento.apply(alias, url, authenticationMethod.memento());
    }

    public ResourceName getDefaultNamespace() {
        return authenticationMethod.getDefaultNamespace().orElse(ResourceName.apply("public"));
    }

    public RepositoriesClient getRepositoriesClient() {
        return repositoriesClientFactory.createRepositories(url, authenticationMethod);
    }

}
