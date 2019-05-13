package ada.vcs.client.commands;

import picocli.CommandLine;

@CommandLine.Command(
    name = "avcs",
    description = "Ada Data Version Control System",
    subcommands = {
        Config.class,
        Dataset.class,
        Datasets.class,
        Endpoint.class,
        Init.class,
        Remote.class,
        Remotes.class,
        Repositories.class,
        Repositories$Create.class,
        Server.class
    })
public final class Root extends StandardOptions {

}
