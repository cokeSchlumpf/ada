package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "targets",
    description = "work with targets of dataset",
    subcommands = {
        Dataset$Targets$Add.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Targets extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private Dataset dataset;

    public static Dataset$Targets apply(CommandLineConsole console) {
        return apply(console, null);
    }

    @Override
    public void run() {
        console.message("Printing some information about existing targets");
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }
}
