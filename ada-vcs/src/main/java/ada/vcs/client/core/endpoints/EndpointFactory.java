package ada.vcs.client.core.endpoints;

import ada.commons.util.ResourceName;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethod;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethodFactory;
import lombok.AllArgsConstructor;

import java.net.URL;

@AllArgsConstructor(staticName = "apply")
public final class EndpointFactory {

    private final AuthenticationMethodFactory authMethodFactory;

    public Endpoint create(ResourceName alias, URL url, AuthenticationMethod method) {
        return Endpoint.apply(alias, url, method);
    }

    public Endpoint create(ResourceName alias, URL url) {
        return create(alias, url, authMethodFactory.none());
    }

    public Endpoint create(EndpointMemento memento) {
        return Endpoint.apply(
            memento.getAlias(), memento.getUrl(),  authMethodFactory.create(memento.getAuthentication()));
    }

}
