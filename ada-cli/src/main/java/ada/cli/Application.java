package ada.cli;

import ada.cli.clients.AboutResourceClient;
import ada.client.Client;
import ada.client.output.DefaultClientOutput;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);

        app
            .run(args)
            .close();
    }

    @Override
    public void run(String... args) {
        ActorSystem system = ActorSystem.create("ada-cli");
        ActorMaterializer materializer = ActorMaterializer.create(system);

        ApplicationContext context = ApplicationContext.apply(
                AboutResourceClient.apply(materializer),
                materializer,
                DefaultClientOutput.apply());

        Client.apply(context).run(args);
        system.terminate();
    }

}
