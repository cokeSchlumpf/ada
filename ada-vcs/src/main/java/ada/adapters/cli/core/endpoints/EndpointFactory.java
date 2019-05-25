package ada.adapters.cli.core.endpoints;

import ada.adapters.client.modifiers.AuthenticationMethod;
import ada.adapters.client.modifiers.AuthenticationMethodFactory;
import ada.adapters.client.repositories.RepositoriesClientFactory;
import ada.commons.util.ResourceName;
import lombok.AllArgsConstructor;

import java.net.URL;

@AllArgsConstructor(staticName = "apply")
public final class EndpointFactory {

    private final AuthenticationMethodFactory authMethodFactory;

    private final RepositoriesClientFactory repositoriesClientFactory;

    public Endpoint create(ResourceName alias, URL url, AuthenticationMethod method) {
        return Endpoint.apply(repositoriesClientFactory, alias, url, method);
    }

    public Endpoint create(ResourceName alias, URL url) {
        return create(alias, url, authMethodFactory.none());
    }

    public Endpoint create(EndpointMemento memento) {
        return create(
            memento.getAlias(),
            memento.getUrl(),
            authMethodFactory.create(memento.getAuthentication()));
    }

}
