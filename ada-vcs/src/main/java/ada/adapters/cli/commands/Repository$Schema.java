package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.CommandNotInitializedException;
import ada.commons.util.Operators;
import ada.adapters.cli.commands.context.CommandContext;
import ada.domain.dvc.values.repository.VersionStatus;
import akka.Done;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@CommandLine.Command(
    name = "schema",
    description = "display the avro schema of a repository")
@AllArgsConstructor(staticName = "apply")
public final class Repository$Schema extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Repository repository;

    @CommandLine.Parameters(
        index = "0",
        arity = "0..1",
        description = "The id of the version, if empty the latest will be displayed")
    private String version;

    public static Repository$Schema apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, null);
    }

    @Override
    public void run() {
        CompletionStage<Void> result = getRepository()
            .getRepositoryDetails()
            .thenAccept(details -> {
                if (details.getVersions().isEmpty()) {
                    console.message("The repository does not contain any version - No schema to display");
                } else if (version == null || version.length() == 0) {
                    display(details.getVersions().get(0));
                } else {
                    details
                        .getVersions()
                        .stream()
                        .filter(s ->
                            s.getDetails().getId().equals(version) || s.getDetails().getId().contains(version))
                        .findFirst()
                        .map(status -> {
                            display(status);
                            return Done.getInstance();
                        })
                        .orElseGet(() -> {
                            console.message("The repository does not contain a version '%s'", version);
                            return Done.getInstance();
                        });
                }
            });

        Operators.suppressExceptions(() -> result.toCompletableFuture().get());
    }

    private void display(VersionStatus status) {
        console.message(status.getDetails().getId());
        console.message("");
        console.message(status.getDetails().getSchema().toString(true));
    }

    public Repository getRepository() {
        return Optional.ofNullable(repository).orElseThrow(CommandNotInitializedException::apply);
    }

}
