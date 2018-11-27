package ada.cli.configuration;

import akka.actor.ActorSystem;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;

/**
 * Contains procedures to be executed during shutdown of the system.
 */
@Controller
public class ShutdownConfiguration {

    private final ActorSystem system;

    public ShutdownConfiguration(ActorSystem system) {
        this.system = system;
    }

    @PreDestroy
    public void shutdown() {
        system.terminate();
    }

}
