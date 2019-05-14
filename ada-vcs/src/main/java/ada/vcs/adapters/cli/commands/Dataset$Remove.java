package ada.vcs.adapters.cli.commands;

import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "remove",
    description = "removes a dataset from the project")
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Remove extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset dataset;

    public static Dataset$Remove apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Dataset dataset = getDataset().orElseThrow(CommandNotInitializedException::apply);
            project.removeDataset(dataset.alias());
            console.message("Removed dataset '%s' from project", dataset.alias());
        });
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
