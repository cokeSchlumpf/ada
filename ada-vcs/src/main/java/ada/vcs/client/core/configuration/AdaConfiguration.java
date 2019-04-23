package ada.vcs.client.core.configuration;

import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.User;

import java.util.Optional;

public interface AdaConfiguration extends Writable {

    Optional<User> getUser();

}
