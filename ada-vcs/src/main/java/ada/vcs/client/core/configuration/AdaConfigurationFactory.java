package ada.vcs.client.core.configuration;

import ada.vcs.client.core.repository.api.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class AdaConfigurationFactory {

    ObjectMapper om;

    public AdaConfiguration create() {
        return AdaConfigurationImpl.apply(om, null);
    }

    public AdaConfiguration create(User user) {
        return AdaConfigurationImpl.apply(om, user);
    }

    public AdaConfiguration create(AdaConfigurationMemento memento) {
        return create(memento.getUser());
    }

    public AdaConfiguration create(InputStream is) throws IOException {
        AdaConfigurationMemento memento = om.readValue(is, AdaConfigurationMemento.class);
        return create(memento);
    }

    public AdaConfiguration create(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return create(is);
        }
    }

}
