package ada.vcs.adapters.cli.commands;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.core.configuration.AdaConfiguration;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import ada.vcs.adapters.cli.exceptions.EndpointNotExistingException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "rename",
    description = "rename an existing endpoint")
@AllArgsConstructor(staticName = "apply")
public class Endpoint$Rename extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private final Endpoint endpoint;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "ALIAS",
        description = "the new alias for the endpoint")
    private String alias = null;

    public static Endpoint$Rename apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, "other");
    }

    @Override
    public void run() {
        context.withAdaHome(home -> {
            AdaConfiguration config = home.getConfiguration();

            ada.vcs.adapters.cli.core.endpoints.Endpoint endpoint = config
                .getEndpoint(getEndpoint().getAlias())
                .orElseThrow(() -> EndpointNotExistingException.apply(getEndpoint().getAlias().getValue()));

            Boolean isCurrent = config
                .getEndpoint()
                .map(ada.vcs.adapters.cli.core.endpoints.Endpoint::getAlias)
                .map(ResourceName::getValue)
                .map(s -> s.equals(getEndpoint().getAlias().getValue()))
                .orElse(false);

            config.removeEndpoint(getEndpoint().getAlias());
            config.addEndpoint(endpoint.withAlias(ResourceName.apply(alias)));

            if (isCurrent) {
                config.setEndpoint(ResourceName.apply(alias));
            }
        });
    }

    public Endpoint getEndpoint() {
        return Optional.ofNullable(endpoint).orElseThrow(CommandNotInitializedException::apply);
    }

}
