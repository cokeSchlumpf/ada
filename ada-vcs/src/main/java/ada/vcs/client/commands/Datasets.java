package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.AdaProject;
import ada.vcs.client.core.Dataset;
import ada.vcs.client.exceptions.NoProjectException;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "datasets",
    description = "work with datasets",
    subcommands = {
        Datasets$Add.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Datasets extends StandardOptions implements Runnable {

    private CommandLineConsole console;

    @Override
    public void run() {
        AdaProject project = AdaProject.fromHere().orElseThrow(NoProjectException::apply);

        List<Dataset> datasets = project
            .getDatasets()
            .sorted()
            .collect(Collectors.toList());

        if (datasets.size() > 0) {
            console.table(
                Lists.newArrayList("Alias", "Type"),
                datasets
                    .stream()
                    .map(ds -> Lists.newArrayList(ds.getAlias().getValue(), ds.getSource().getInfo()))
                    .collect(Collectors.toList()),
                true);
        } else {
            console.message("No datasets in project.");
        }
    }

}
