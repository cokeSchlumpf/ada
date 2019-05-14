package ada.vcs.adapters.cli.commands;

import lombok.AllArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "add",
    description = "adds a new dataset to version control",
    subcommands = {
        Datasets$Add$CSV.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Datasets$Add extends StandardOptions {

}
