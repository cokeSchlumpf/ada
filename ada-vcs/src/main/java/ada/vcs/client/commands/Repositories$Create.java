package ada.vcs.client.commands;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.server.adapters.client.repositories.RepositoryClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.concurrent.CompletionStage;

@CommandLine.Command(
    name = "create",
    description = "creates a new repository on Ada server")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Repositories$Create extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "NAME",
        description = "the name of the repository")
    private String repository = null;

    public static Repositories$Create apply(CommandLineConsole console, CommandContext context) {
        return new Repositories$Create(console, context, null);
    }

    @Override
    public void run() {
        context.withEndpoint(endpoint -> {
            FQResourceName name = FQResourceName
                .tryApply(repository)
                .orElse(FQResourceName.apply(endpoint.getDefaultNamespace(), ResourceName.apply(repository)));

            CompletionStage<RepositoryClient> result = endpoint
                .getRepositoriesClient()
                .createRepository(name.getNamespace(), name.getName());

            Operators.suppressExceptions(() -> result.toCompletableFuture().get());

            console.message("Created repository '%s'", name);
        });
    }

}
