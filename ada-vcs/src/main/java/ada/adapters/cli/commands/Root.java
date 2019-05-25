package ada.adapters.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(
    name = "avcs",
    description = "Ada Data Version Control System",
    subcommands = {
        Config.class,
        Dataset.class,
        Datasets.class,
        Endpoint.class,
        Endpoints.class,
        Init.class,
        Remote.class,
        Remotes.class,
        Repositories.class,
        Repository.class,
        Server.class
    })
public final class Root extends StandardOptions {

}
