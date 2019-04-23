package ada.vcs.client.core.configuration;

import ada.vcs.client.core.repository.api.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

@Value
@AllArgsConstructor(staticName = "apply")
public final class AdaConfigurationImpl implements AdaConfiguration {

    ObjectMapper om;

    User user;

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, AdaConfigurationMemento.apply(user));
    }
}
