package ada.vcs.client.commands;

import lombok.AllArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "add",
    description = "adds a new target to the dataset",
    subcommands = {
        Dataset$Targets$Add$CSV.class,
        Dataset$Targets$Add$Local.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Targets$Add extends StandardOptions {

}
