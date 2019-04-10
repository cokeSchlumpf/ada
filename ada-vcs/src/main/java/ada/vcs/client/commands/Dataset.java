package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "dataset",
    aliases = { "ds" },
    description = "work with a specific dataset",
    subcommands = {
        Dataset$Extract.class,
        Dataset$Targets.class
    })
@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class Dataset extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.Parameters(index = "0", description = "The alias of the dataset")
    private String alias;

    public static Dataset apply(CommandLineConsole console) {
        return apply(console, null);
    }

    @Override
    public void run() {
        console.message("Printing some information about datasets");
    }

    public String getAlias() {
        return alias;
    }
}
