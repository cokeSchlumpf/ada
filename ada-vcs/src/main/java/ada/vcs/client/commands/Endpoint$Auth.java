package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.AdaHome;
import ada.vcs.client.exceptions.CommandNotInitializedException;
import ada.vcs.client.exceptions.EndpointNotExistingException;
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

    public void withEndpoint(BiConsumer<AdaHome, ada.vcs.client.core.endpoints.Endpoint> f) {
        context.withAdaHome(home -> {
            ada.vcs.client.core.endpoints.Endpoint endpoint = home
                .getConfiguration()
                .getEndpoint(getEndpoint().getAlias())
                .orElseThrow(() -> EndpointNotExistingException.apply(this.endpoint.getAlias().getValue()));

            f.accept(home, endpoint);
        });
    }

}
