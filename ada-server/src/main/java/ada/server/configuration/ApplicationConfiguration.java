package ada.server.configuration;

import ada.web.impl.resources.about.AboutConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class contains configuration values which can be read/ set by Spring from application.yml file.
 */
@Configuration
@ConfigurationProperties(prefix = "ada")
public class ApplicationConfiguration implements AboutConfiguration {

    private String name;
    private String build;


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBuild() {
        return build;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setBuild(String build) {
        this.build = build;
    }

}
