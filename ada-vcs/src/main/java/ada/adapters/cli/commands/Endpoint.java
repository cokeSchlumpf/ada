package ada.adapters.cli.commands;

import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.EndpointNotExistingException;
import ada.commons.util.ResourceName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "endpoint",
    description = "work with a specific endpoint",
    subcommands = {
        Endpoint$Auth.class,
        Endpoint$Remove.class,
        Endpoint$Rename.class
    })
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Endpoint extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(index = "0", description = "The alias of the remote")
    private String alias;

    public static Endpoint apply(CommandLineConsole console, CommandContext context) {
        return new Endpoint(console, context, null);
    }

    @Override
    public void run() {
        context.withAdaHome(home -> {
            ResourceName a = ResourceName.apply(alias);
            ada.adapters.cli.core.endpoints.Endpoint endpoint = home
                .getConfiguration()
                .getEndpoint(a)
                .orElseThrow(() -> EndpointNotExistingException.apply(alias));

            String currentInfo = home
                .getConfiguration()
                .getEndpoint()
                .filter(e -> e.getAlias().equals(a))
                .map(e -> "* ")
                .orElse("  ");

            console.message("%s%s", currentInfo, endpoint.getUrl());
        });
    }

    public ResourceName getAlias() {
        return ResourceName.apply(alias);
    }

}
