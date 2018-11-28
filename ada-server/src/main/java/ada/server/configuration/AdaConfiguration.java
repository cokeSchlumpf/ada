package ada.server.configuration;

import ada.web.api.resources.about.AboutResource;
import ada.web.impl.resources.about.AboutResourceFactory;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class implements factories to instantiate dependent classes from Ada (and libraries around, e.g. Akka).
 */
@Configuration
public class AdaConfiguration {

    private final ApplicationConfiguration config;

    public AdaConfiguration(ApplicationConfiguration config) {
        this.config = config;
    }

    @Bean
    public AboutResource getAboutResource(Materializer materializer) {
        return AboutResourceFactory.create(config, materializer);
    }

    @Bean
    public ActorSystem getAkkaActorSystem() {
        return ActorSystem.create("ada-server");
    }

    @Bean
    public Materializer getAkkaMaterializer(ActorSystem system) {
        return ActorMaterializer.create(system);
    }


}
