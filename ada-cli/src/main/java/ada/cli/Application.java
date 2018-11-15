package ada.cli;

import ada.cli.clients.AboutResourceClient;
import ada.client.CliApplicationBuilder;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private final ActorSystem system;

    private final ApplicationContext context;

    public Application(ActorSystem system, ApplicationContext context) {
        this.system = system;
        this.context = context;
    }

    public static void main(String... args) {
        SpringApplication.run(Application.class, "about");
    }

    @Override
    public void run(String... args) {
        ActorMaterializer mat = ActorMaterializer.create(system);

        CliApplicationBuilder
                .apply(mat)
                .withAboutCommand(AboutResourceClient.apply(mat))
                .withArguments(args)
                .run();
    }

}
