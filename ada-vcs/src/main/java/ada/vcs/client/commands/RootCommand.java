package ada.vcs.client.commands;

import picocli.CommandLine;

@CommandLine.Command(
    name = "avcs",
    description = "Ada Data Version Control System",
    subcommands = {
        InitCommand.class
    })
public final class RootCommand extends StandardOptions {

}
