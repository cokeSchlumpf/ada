package ada.vcs.adapters.cli.commands;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.core.endpoints.Endpoint;
import ada.vcs.domain.dvc.protocol.queries.RepositoryDetailsResponse;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.ocpsoft.prettytime.PrettyTime;
import picocli.CommandLine;

import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "repository",
    description = "work with a single repository on Ada server",
    subcommands = {
        Repository$Grant.class,
        Repository$Grants.class,
        Repository$Remove.class,
        Repository$Revoke.class,
        Repository$Schema.class
    })
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
            PrettyTime pt = new PrettyTime();

            CompletionStage<Void> result = getRepositoryDetails()
                .thenAccept(details -> {
                    console.table(
                        Lists.newArrayList("Id", "Date", "State", "User"),
                        details
                            .getVersions()
                            .stream()
                            .map(version -> Lists.newArrayList(
                                version.getDetails().getId(),
                                pt.format(version.getDetails().getDate()),
                                version.getState().toString().toLowerCase(),
                                version.getDetails().getUser().getName()
                            ))
                            .collect(Collectors.toList()),
                        true);
                });

            Operators.suppressExceptions(() -> result.toCompletableFuture().get());
        });
    }

    public CompletionStage<RepositoryDetailsResponse> getRepositoryDetails() {
        return context.fromEndpoint(endpoint -> {
            FQResourceName fqn = getRepositoryName();

            return endpoint
                .getRepositoriesClient()
                .getRepository(fqn.getNamespace(), fqn.getName())
                .details();
        });
    }

    public FQResourceName getRepositoryName(Endpoint endpoint) {
        return FQResourceName
            .tryApply(name)
            .orElse(FQResourceName.apply(endpoint.getDefaultNamespace(), ResourceName.apply(name)));
    }

    public FQResourceName getRepositoryName() {
        return context.fromEndpoint(this::getRepositoryName);
    }

}
