package ada.server.configuration;

import ada.server.web.impl.resources.about.AboutControllerConfiguration;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@Configuration
public class DefaultConfiguration implements AboutControllerConfiguration {

    @Value("${ada.application.name:ada}")
    private String name;

    @Value("${ada.application.build:0.0.0}")
    private String build;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBuild() {
        return build;
    }

    @Bean
    public ActorSystem getActorSystem() {
        return ActorSystem.create(getName());
    }

}
