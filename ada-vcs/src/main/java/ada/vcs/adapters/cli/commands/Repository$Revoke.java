package ada.vcs.adapters.cli.commands;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.commands.enums.EAuthType;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import ada.vcs.adapters.cli.exceptions.ExitWithErrorException;
import ada.vcs.domain.dvc.protocol.values.Authorization;
import ada.vcs.domain.dvc.protocol.values.RoleAuthorization;
import ada.vcs.domain.dvc.protocol.values.UserAuthorization;
import ada.vcs.domain.dvc.protocol.values.WildcardAuthorization;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@CommandLine.Command(
    name = "revoke",
    description = "revoke access from the repository")
@AllArgsConstructor(staticName = "apply")
public final class Repository$Revoke extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Repository repository;

    @CommandLine.Parameters(
        index = "0",
        description = "Either 'user', 'role' or '*'")
    private EAuthType type;

    @CommandLine.Parameters(
        index = "1",
        description = "In case of 'user' or 'role': The name of the user/role"
    )
    private String name;

    public static Repository$Revoke apply(CommandLineConsole console, CommandContext context) {
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
                .revoke(auth)
                .thenAccept(granted -> console.message("Revoked access to repository."));
        });

        Operators.suppressExceptions(() -> done.toCompletableFuture().get());
    }

    private Repository getRepository() {
        return Optional.ofNullable(repository).orElseThrow(CommandNotInitializedException::apply);
    }

}
