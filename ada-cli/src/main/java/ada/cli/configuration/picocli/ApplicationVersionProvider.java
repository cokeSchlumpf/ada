package ada.cli.configuration.picocli;

import ada.cli.configuration.ApplicationConfiguration;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class ApplicationVersionProvider implements CommandLine.IVersionProvider {

    private final ApplicationConfiguration configuration;

    public ApplicationVersionProvider(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String[] getVersion() {
        return new String[]{"Ada Command Line Interface",
            String.format("Build: %s", configuration.getBuild())};
    }

}
