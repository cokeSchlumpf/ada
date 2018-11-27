package ada.cli.commands;

import ada.cli.commands.about.AboutCommandAnnotations;
import picocli.CommandLine;

@CommandLine.Command(
    name = "ada",
    mixinStandardHelpOptions = true,
    description = "Ada Command Line Interface for Data Scientists",
    subcommands = {
        AboutCommandAnnotations.class
    })
public class AdaCommand {
}
