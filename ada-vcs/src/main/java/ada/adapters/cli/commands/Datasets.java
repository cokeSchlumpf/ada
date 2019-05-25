package ada.adapters.cli.commands;

import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.core.dataset.Dataset;
import ada.adapters.cli.exceptions.ExitWithErrorException;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "datasets",
    description = "work with datasets",
    subcommands = {
        Datasets$Add.class,
        Datasets$Extract.class,
        Datasets$Push.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Datasets extends StandardOptions implements Runnable {

    private CommandLineConsole console;

    private CommandContext context;

    @Override
    public void run() {
        context.withProject(project -> {
            List<Dataset> datasets = project
                .getDatasets()
                .sorted()
                .collect(Collectors.toList());

            if (datasets.size() > 0) {
                console.table(
                    Lists.newArrayList("Alias", "Location"),
                    datasets
                        .stream()
                        .map(ds -> Lists.newArrayList(ds.alias().getValue(), ds.source().info()))
                        .collect(Collectors.toList()),
                    true);
            } else {
                throw ExitWithErrorException.apply("No datasets in project.");
            }
        });
    }

}
