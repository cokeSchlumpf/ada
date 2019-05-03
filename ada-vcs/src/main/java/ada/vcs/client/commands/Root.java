package ada.vcs.client.commands;

import picocli.CommandLine;

@CommandLine.Command(
    name = "avcs",
    description = "Ada Data Version Control System",
    subcommands = {
        Config.class,
        Dataset.class,
        Datasets.class,
        Init.class,
        Remote.class,
        Remotes.class,
        Server.class
    })
public final class Root extends StandardOptions {

}
