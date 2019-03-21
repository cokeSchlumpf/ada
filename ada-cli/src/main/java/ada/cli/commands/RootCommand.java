package ada.cli.commands;

import ada.cli.commands.about.AboutCommand;
import ada.cli.commands.repository.commands.RepositoryCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "ada",
    description = "Ada Command Line Interface for Data Scientists",
    subcommands = {
        AboutCommand.class,
        RepositoryCommand.class
    })
public class RootCommand extends StandardOptions {

}
