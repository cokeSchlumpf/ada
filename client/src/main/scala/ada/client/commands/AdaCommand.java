package ada.client.commands;

import ada.client.commands.about.AboutCommandPicoDecorator;
import picocli.CommandLine;

@CommandLine.Command(
        name = "ada",
        mixinStandardHelpOptions = true,
        description = "CLI ada.client for IBM Ada.",
        subcommands = {
                AboutCommandPicoDecorator.class
        })
public final class AdaCommand {

}
