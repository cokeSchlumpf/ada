package ada.cli.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class contains configuration values which can be read/ set by Spring from application.yml file.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "pythagoras")
public class ApplicationConfiguration {

    private String build;

    private Server server;

    @Getter
    @Setter
    public static class Server {

        private String baseUrl;

    }

}
