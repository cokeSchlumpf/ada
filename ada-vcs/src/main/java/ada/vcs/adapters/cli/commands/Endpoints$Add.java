package ada.vcs.adapters.cli.commands;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.net.URL;

@CommandLine.Command(
    name = "add",
    description = "adds a new endpoint")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Endpoints$Add extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "URL",
        description = "the base URL of the endpoint")
    private URL url = null;

    @CommandLine.Parameters(
        index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the endpoint, if not set the hostname of the endpoint will be used")
    private String alias = null;

    public static Endpoints$Add apply(CommandLineConsole console, CommandContext context) {
        return new Endpoints$Add(console, context, null, null);
    }

    @Override
    public void run() {
        context.withAdaHome(home -> {
            if (alias == null || alias.trim().length() == 0) {
                alias = url.getHost();
            }

            home.getConfiguration().addEndpoint(context
                .factories()
                .endpointFactory()
                .create(ResourceName.apply(alias), url));
        });
    }

}
