package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.exceptions.CommandNotInitializedException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "rename",
    description = "renames an existing dataset")
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Rename  extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset dataset;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "ALIAS",
        description = "the new alias for the dataset")
    private String alias = null;

    public static Dataset$Rename apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, null);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Dataset dataset = getDataset().orElseThrow(CommandNotInitializedException::apply);

            ada.vcs.client.core.dataset.Dataset ds$new = project
                .getDataset(dataset.alias())
                .withAlias(ResourceName.apply(alias));

            project.addDataset(ds$new);
            project.removeDataset(dataset.alias());

            console.message("Renamed dataset '%s' to '%s'", dataset.alias(), ds$new.alias().getValue());
        });
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
