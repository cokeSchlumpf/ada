package ada.cli.commands;

import ada.cli.commands.about.AboutCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "ada",
    description = "Ada Command Line Interface for Data Scientists",
    subcommands = {
        AboutCommand.class
    })
public class AdaCommand extends StandardOptions {

}
