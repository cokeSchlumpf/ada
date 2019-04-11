package ada.vcs.client.commands;

import picocli.CommandLine;

@CommandLine.Command(
    name = "avcs",
    description = "Ada Data Version Control System",
    subcommands = {
        Dataset.class,
        Datasets.class,
        Init.class,
        Remotes.class
    })
public final class Root extends StandardOptions {

}
