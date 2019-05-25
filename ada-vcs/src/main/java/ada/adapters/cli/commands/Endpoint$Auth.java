package ada.adapters.cli.commands;

import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.core.AdaHome;
import ada.adapters.cli.exceptions.CommandNotInitializedException;
import ada.adapters.cli.exceptions.EndpointNotExistingException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;
import java.util.function.BiConsumer;

@CommandLine.Command(
    name = "auth",
    description = "configure or show authentication for endpoint",
    subcommands = {
        Endpoint$Auth$API.class,
        Endpoint$Auth$None.class,
        Endpoint$Auth$Stupid.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Endpoint$Auth extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Endpoint endpoint;

    public static Endpoint$Auth apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        withEndpoint((home, endpoint) -> console.message(endpoint.getAuthenticationMethod().info()));
    }

    public Endpoint getEndpoint() {
        return Optional.ofNullable(endpoint).orElseThrow(CommandNotInitializedException::apply);
    }

    public void withEndpoint(BiConsumer<AdaHome, ada.adapters.cli.core.endpoints.Endpoint> f) {
        context.withAdaHome(home -> {
            ada.adapters.cli.core.endpoints.Endpoint endpoint = home
                .getConfiguration()
                .getEndpoint(getEndpoint().getAlias())
                .orElseThrow(() -> EndpointNotExistingException.apply(this.endpoint.getAlias().getValue()));

            f.accept(home, endpoint);
        });
    }

}
