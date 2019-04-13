package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.Target;
import ada.vcs.client.core.project.AdaProject;
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
public final class Dataset$Targets extends StandardOptions implements ProjectCommand {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private Dataset dataset;

    public static Dataset$Targets apply(CommandLineConsole console) {
        return apply(console, null);
    }

    @Override
    public void run(AdaProject project) {
        final Dataset dataset = getDataset().orElseThrow(RuntimeException::new);

        List<Target> targets = project
            .getTargets(dataset.getAlias())
            .sorted()
            .collect(Collectors.toList());

        if (targets.isEmpty()) {
            console.message("Dataset '%s' does not contain any targets.", dataset.getAlias());
        } else {
            console.table(
                Lists.newArrayList("Alias", "Type"),
                targets
                    .stream()
                    .map(target -> Lists.newArrayList(target.getAlias().getValue(), target.getSink().getInfo()))
                    .collect(Collectors.toList()),
                true);
        }
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
