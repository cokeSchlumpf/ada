package ada.vcs.adapters.cli.commands;

import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.core.dataset.Target;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "target",
    description = "work with a specific target from the dataset",
    subcommands = {
        Dataset$Target$Remove.class,
        Dataset$Target$Rename.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Target extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset dataset;

    @CommandLine.Parameters(index = "0", description = "The alias of the target")
    private String alias;

    public static Dataset$Target apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, "");
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Dataset dataset = getDataset().orElseThrow(RuntimeException::new);
            Target target = project.getTarget(dataset.alias(), alias);

            console.table(Lists.newArrayList(Pair.of("Alias", target.alias()), Pair.of("Type", target.sink().info())));
        });
    }

    public String getAlias() {
        return alias;
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
