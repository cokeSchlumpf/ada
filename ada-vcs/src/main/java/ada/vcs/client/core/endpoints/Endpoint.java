package ada.vcs.client.core.endpoints;

import ada.commons.util.ResourceName;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethod;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URL;

@Value
@Wither
@AllArgsConstructor(staticName = "apply")
public class Endpoint {

    private final ResourceName alias;

    private final URL url;

    private final AuthenticationMethod authenticationMethod;

    public EndpointMemento memento() {
        return EndpointMemento.apply(alias, url, authenticationMethod.memento());
    }

}
