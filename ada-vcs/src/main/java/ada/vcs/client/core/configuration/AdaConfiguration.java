package ada.vcs.client.core.configuration;

import ada.commons.io.Writable;
import ada.vcs.shared.repository.api.User;

import java.util.Optional;

public interface AdaConfiguration extends Writable {

    Optional<User> getUser();

}
