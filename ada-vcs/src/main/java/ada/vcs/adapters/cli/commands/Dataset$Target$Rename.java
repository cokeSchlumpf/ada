package ada.vcs.adapters.cli.commands;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "rename",
    description = "renames a target from the dataset")
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Target$Rename extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset$Target target = null;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "ALIAS",
        description = "the new alias for the target")
    private String alias = null;

    public static Dataset$Target$Rename apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, "");
    }

    @Override
    public void run() {
        context.withProject(project -> {
            Dataset dataset = getTarget()
                .flatMap(Dataset$Target::getDataset)
                .orElseThrow(CommandNotInitializedException::apply);

            Dataset$Target target = getTarget()
                .orElseThrow(CommandNotInitializedException::apply);

            project.addTarget(
                dataset.alias(),
                project
                    .getTarget(
                        dataset.alias(),
                        target.getAlias())
                    .withAlias(ResourceName.apply(alias)));

            project.removeTarget(dataset.alias(), target.getAlias());
            console.message("Renamed target '%s' to '%s'", target.getAlias(), alias);
        });
    }

    public Optional<Dataset$Target> getTarget() {
        return Optional.ofNullable(target);
    }

}
