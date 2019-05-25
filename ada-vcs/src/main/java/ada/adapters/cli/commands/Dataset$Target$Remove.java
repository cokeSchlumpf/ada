package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.CommandNotInitializedException;
import ada.adapters.cli.commands.context.CommandContext;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "remove",
    description = "removes a target from the dataset")
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Target$Remove extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset$Target target = null;

    public static Dataset$Target$Remove apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            Dataset dataset = getTarget()
                .flatMap(Dataset$Target::getDataset)
                .orElseThrow(CommandNotInitializedException::apply);

            Dataset$Target target = getTarget()
                .orElseThrow(CommandNotInitializedException::apply);

            project.removeTarget(dataset.alias(), target.getAlias());
            console.message("Removed target '%s' from dataset '%s'", target.getAlias(), dataset.alias());
        });
    }

    public Optional<Dataset$Target> getTarget() {
        return Optional.ofNullable(target);
    }

}
