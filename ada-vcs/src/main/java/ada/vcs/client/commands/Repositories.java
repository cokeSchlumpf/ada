package ada.vcs.client.commands;

import ada.commons.util.Operators;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesResponse;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.ocpsoft.prettytime.PrettyTime;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "repositories",
    description = "list or add repositories from Ada server",
    subcommands = {
        Repositories$Create.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Repositories extends StandardOptions implements Runnable {

    private CommandLineConsole console;

    private CommandContext context;

    @Override
    public void run() {
        context.withEndpoint(endpoint -> {
            PrettyTime pt = new PrettyTime();

            CompletionStage<Void> run = endpoint
                .getRepositoriesClient()
                .listRepositories()
                .thenApply(RepositoriesResponse::getRepositories)
                .thenAccept(repositories -> {
                    List<List<String>> data = repositories
                        .stream()
                        .map(summary -> Lists.newArrayList(
                            String.format("%s/%s", summary.getNamespace().getValue(), summary.getRepository().getValue()),
                            pt.format(summary.getLastUpdate()),
                            summary.getLatestId().orElse("")))
                        .collect(Collectors.toList());

                    console.table(
                        Lists.newArrayList("Name", "Last Update", "Latest"),
                        data,
                        true);
                });

            Operators.suppressExceptions(() -> run.toCompletableFuture().get());
        });
    }

}
