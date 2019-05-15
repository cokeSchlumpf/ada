package ada.vcs.adapters.cli.commands;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.concurrent.CompletionStage;

@CommandLine.Command(
    name = "repository",
    description = "work with a single repository on Ada server")
@AllArgsConstructor(staticName = "apply")
public final class Repository extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(index = "0", description = "The name of the repository")
    private String name;

    public static Repository apply(CommandLineConsole console, CommandContext context) {
        return Repository.apply(console, context, null);
    }

    @Override
    public void run() {
        context.withEndpoint(endpoint -> {
            FQResourceName fqn = FQResourceName
                .tryApply(name)
                .orElse(FQResourceName.apply(endpoint.getDefaultNamespace(), ResourceName.apply(name)));

            CompletionStage<Void> result = endpoint
                .getRepositoriesClient()
                .getRepository(fqn.getNamespace(), fqn.getName())
                .details()
                .thenAccept(details -> {
                    String s = Operators.suppressExceptions(() -> context.factories().objectMapper().writeValueAsString(details));
                    console.message(s);
                });

            Operators.suppressExceptions(() -> result.toCompletableFuture().get());
        });
    }

}
