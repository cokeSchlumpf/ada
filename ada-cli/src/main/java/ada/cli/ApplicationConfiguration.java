package ada.cli;

import akka.actor.ActorSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ActorSystem getActorSystem() {
        return ActorSystem.create("ada-cli");
    }

}
