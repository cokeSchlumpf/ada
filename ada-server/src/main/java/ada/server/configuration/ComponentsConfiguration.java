package ada.server.configuration;

import ada.server.repository.filesystem.FSRepositories;
import com.ibm.ada.api.repository.Repositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class implements factories to instantiate dependent classes from Ada (and libraries around, e.g. Akka).
 */
@Configuration
public class ComponentsConfiguration {

    private final ApplicationConfiguration config;

    public ComponentsConfiguration(ApplicationConfiguration config) {
        this.config = config;
    }

    @Bean
    public Repositories repository() {
        return FSRepositories.apply();
    }

}
