package ada.vcs.adapters.cli.commands;

import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "remove",
    description = "removes a remote from configuration")
@AllArgsConstructor(staticName = "apply")
public final class Endpoint$Remove extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Endpoint endpoint;

    public static Endpoint$Remove apply(CommandLineConsole console, CommandContext context) {
        return new Endpoint$Remove(console, context, null);
    }

    @Override
    public void run() {
        context.withAdaHome(home -> {
            final Endpoint endpoint = getEndpoint().orElseThrow(CommandNotInitializedException::apply);
            home.getConfiguration().removeEndpoint(endpoint.getAlias());
            console.message("Removed endpoint '%s'", endpoint.getAlias().getValue());
        });
    }

    public Optional<Endpoint> getEndpoint() {
        return Optional.ofNullable(endpoint);
    }

}
