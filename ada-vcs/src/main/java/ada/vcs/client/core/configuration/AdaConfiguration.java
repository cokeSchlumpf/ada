package ada.vcs.client.core.configuration;

import ada.commons.io.Writable;
import ada.commons.util.ResourceName;
import ada.vcs.client.core.endpoints.Endpoint;
import ada.vcs.shared.repository.api.User;

import java.util.List;
import java.util.Optional;

public interface AdaConfiguration extends Writable {

    Optional<Endpoint> getEndpoint();

    Optional<Endpoint> getEndpoint(ResourceName alias);

    Optional<User> getUser();

    void addEndpoint(Endpoint endpoint);

    List<Endpoint> getEndpoints();

    void setEndpoint(ResourceName alias);

    void setUser(User user);

    void removeEndpoint(ResourceName alias);

    void unsetUser();

}
