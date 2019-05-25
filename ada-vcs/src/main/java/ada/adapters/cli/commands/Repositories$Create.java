package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.adapters.cli.commands.context.CommandContext;
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

    @CommandLine.Option(
        names = { "--add-remote" },
        description = "Add the created repository to the current project as remote.")
    private boolean addRemote;

    public static Repositories$Create apply(CommandLineConsole console, CommandContext context) {
        return new Repositories$Create(console, context, null, false);
    }

    @Override
    public void run() {
        context.withEndpoint(endpoint -> {
            FQResourceName name = FQResourceName
                .tryApply(repository)
                .orElse(FQResourceName.apply(endpoint.getDefaultNamespace(), ResourceName.apply(repository)));

            CompletionStage<Void> result = endpoint
                .getRepositoriesClient()
                .createRepository(name.getNamespace(), name.getName())
                .thenAccept(client -> {
                    console.message("Created repository '%s'", name);

                    if (addRemote) {
                        context.withProject(project -> {
                            project.addRemote(context
                                .factories()
                                .remotesFactory()
                                .createHttpRemote(name.getName(), client.getEndpoint()));

                            console.message("Added %s as remote '%s'", client.getEndpoint(), name.getName());
                        });
                    } else {
                        console.message("Run `avcs remotes add %s` to add the repository to an existing project", client.getEndpoint());
                    }
                });

            Operators.suppressExceptions(() -> result.toCompletableFuture().get());
        });
    }

}
