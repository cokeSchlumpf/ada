package ada.vcs.client.core;

import ada.commons.util.Operators;
import ada.vcs.client.core.configuration.AdaConfiguration;
import ada.vcs.client.core.configuration.AdaConfigurationFactory;
import lombok.AllArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor(staticName = "apply")
public final class AdaHome {

    private static final String ADA_HOME = "ADA_HOME";

    private static final String CONFIGURATION_FILE = "config.json";

    private final AdaConfigurationFactory configurationFactory;

    private final Path dir;

    public static AdaHome apply(AdaConfigurationFactory configurationFactory) {
        Path home;

        if (System.getProperty(ADA_HOME) != null) {
            home = Paths.get(System.getProperty(ADA_HOME));
        } else {
            home = Paths.get(System.getProperty("user.home"));
        }

        return apply(configurationFactory, home);
    }

    public AdaConfiguration getConfiguration() {
        Path file = dir.resolve(CONFIGURATION_FILE);

        if (Files.exists(file)) {
           return Operators.suppressExceptions(() -> configurationFactory.create(file));
        } else {
            return configurationFactory.create();
        }
    }

    public void updateConfiguration(AdaConfiguration configuration) {
        Operators.suppressExceptions(() -> configuration.writeTo(dir.resolve(CONFIGURATION_FILE)));
    }

}
