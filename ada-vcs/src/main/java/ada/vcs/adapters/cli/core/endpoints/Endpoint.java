package ada.vcs.adapters.cli.core.endpoints;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.client.modifiers.AuthenticationMethod;
import ada.vcs.adapters.client.repositories.RepositoriesClient;
import ada.vcs.adapters.client.repositories.RepositoriesClientFactory;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URL;

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
