package ada.vcs.adapters.cli.core.configuration;

import ada.commons.io.Writable;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.core.endpoints.Endpoint;
import ada.vcs.domain.shared.repository.api.User;

import java.util.List;
import java.util.Optional;

public interface AdaConfiguration extends Writable {

    Optional<Endpoint> getEndpoint();

    Optional<Endpoint> getEndpoint(ResourceName alias);

    Optional<ResourceName> getNamespace();

    Optional<User> getUser();

    void addEndpoint(Endpoint endpoint);

    List<Endpoint> getEndpoints();

    void setEndpoint(ResourceName alias);

    void setNamespace(ResourceName namespace);

    void setUser(User user);

    void removeEndpoint(ResourceName alias);

    void unsetNamespace();

    void unsetUser();

}
