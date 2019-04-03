package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "targets",
    description = "work with targets of dataset",
    subcommands = {
        Dataset$Targets$Add.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Targets extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @Override
    public void run() {
        console.message("Printing some information about existing targets");
    }
}
