package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.ExitWithErrorException;
import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.core.dataset.Target;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "targets",
    description = "work with targets of dataset",
    subcommands = {
        Dataset$Targets$Add.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Targets extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset dataset;

    public static Dataset$Targets apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Dataset dataset = getDataset().orElseThrow(RuntimeException::new);

            List<Target> targets = project
                .getTargets(dataset.alias())
                .sorted()
                .collect(Collectors.toList());

            if (targets.isEmpty()) {
                throw ExitWithErrorException.apply("Dataset '%s' does not contain any targets.", dataset.alias());
            } else {
                console.table(
                    Lists.newArrayList("Alias", "Type"),
                    targets
                        .stream()
                        .map(target -> Lists.newArrayList(target.alias().getValue(), target.sink().info()))
                        .collect(Collectors.toList()),
                    true);
            }
        });
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
