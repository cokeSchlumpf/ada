package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.dataset.Dataset;
import ada.vcs.client.exceptions.ExitWithErrorException;
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
