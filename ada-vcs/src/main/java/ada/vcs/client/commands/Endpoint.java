package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.exceptions.EndpointNotExistingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

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
            ada.vcs.client.core.endpoints.Endpoint endpoint = home
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
