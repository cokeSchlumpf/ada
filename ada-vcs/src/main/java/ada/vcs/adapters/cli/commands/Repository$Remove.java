package ada.vcs.adapters.cli.commands;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@CommandLine.Command(
    name = "remove",
    description = "revoke access from the repository")
@AllArgsConstructor(staticName = "apply")
public final class Repository$Remove extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Repository repository;

    public static Repository$Remove apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        CompletionStage<Void> result = context.fromEndpoint(endpoint -> {
            FQResourceName fqn = getRepository().getRepositoryName();

            return endpoint
                .getRepositoriesClient()
                .getRepository(fqn.getNamespace(), fqn.getName())
                .remove()
                .thenAccept(removed -> console.message("The repository has been removed"));
        });

        Operators.suppressExceptions(() -> result.toCompletableFuture().get());
    }

    private Repository getRepository() {
        return Optional.ofNullable(repository).orElseThrow(CommandNotInitializedException::apply);
    }

}
