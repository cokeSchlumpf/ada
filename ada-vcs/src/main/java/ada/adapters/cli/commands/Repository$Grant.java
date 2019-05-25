package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.CommandNotInitializedException;
import ada.adapters.cli.exceptions.ExitWithErrorException;
import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.commands.enums.EAuthType;
import ada.domain.dvc.values.Authorization;
import ada.domain.dvc.values.RoleAuthorization;
import ada.domain.dvc.values.UserAuthorization;
import ada.domain.dvc.values.WildcardAuthorization;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@CommandLine.Command(
    name = "grant",
    description = "grant access to the repository")
@AllArgsConstructor(staticName = "apply")
public final class Repository$Grant extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Repository repository;

    @CommandLine.Parameters(
        index = "0",
        description = "Either 'user', 'role' or 'all'")
    private EAuthType type;

    @CommandLine.Parameters(
        index = "1",
        description = "In case of 'user' or 'role': The name of the user/role"
    )
    private String name;

    public static Repository$Grant apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, null, null);
    }

    @Override
    public void run() {
        Authorization auth;

        switch (type) {
            case USER:
                if (name == null) {
                    throw ExitWithErrorException.apply("'name' must be supplied for user authorization");
                }

                auth = UserAuthorization.apply(name);
                break;

            case ROLE:
                if (name == null) {
                    throw ExitWithErrorException.apply("'name' must be supplied for role authorization");
                }

                auth = RoleAuthorization.apply(name);
                break;

            default:
                auth = WildcardAuthorization.apply();
        }

        CompletionStage<Void> done = context.fromEndpoint(endpoint -> {
            FQResourceName fqn = getRepository().getRepositoryName();
            return endpoint
                .getRepositoriesClient()
                .getRepository(fqn.getNamespace(), fqn.getName())
                .grant(auth)
                .thenAccept(granted -> console.message("Granted access to repository."));
        });

        Operators.suppressExceptions(() -> done.toCompletableFuture().get());
    }

    private Repository getRepository() {
        return Optional.ofNullable(repository).orElseThrow(CommandNotInitializedException::apply);
    }

}
