package ada.cli.commands.about;

import ada.cli.configuration.ApplicationConfiguration;
import ada.client.commands.about.AboutCommand;
import ada.client.commands.about.AboutCommandImpl;
import ada.client.output.Output;
import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.User;
import akka.stream.Materializer;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@CommandLine.Command(
    name = "about",
    mixinStandardHelpOptions = true,
    description = "Shows information about Pythagoras")
@Component
public class AboutCommandAnnotations implements AboutCommand, Runnable {

    @CommandLine.Option(
        names = {"-u", "--user"},
        description = "Show information about authenticated user")
    private boolean user = false;

    @CommandLine.Option(
        names = {"-c", "--client", "--cli"},
        description = "Show information about the Pythagoras CLI client"
    )
    private boolean client = false;

    private final AboutCommand impl;

    private final ApplicationConfiguration configuration;

    private final Output output;

    public AboutCommandAnnotations(
        Output out, User user, Materializer materializer, AboutResource aboutResource,
        ApplicationConfiguration configuration) {

        this.configuration = configuration;
        this.impl = AboutCommandImpl.apply(aboutResource, materializer, out, user);
        this.output = out;
    }


    @Override
    public void about() {
        impl.about();
    }

    @Override
    public void aboutUser() {
        impl.aboutUser();
    }

    @Override
    public void run() {
        about();

        if (client) {
            output.separator();
            output.message("Pythagoras Client Interface");
            output.message("Build: %s", configuration.getBuild());
        }

        if (user) {
            output.separator();
            aboutUser();
        }
    }

}
