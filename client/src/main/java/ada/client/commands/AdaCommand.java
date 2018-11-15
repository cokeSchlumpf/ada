package ada.client.commands;

import picocli.CommandLine;

@CommandLine.Command(
        name = "ada",
        mixinStandardHelpOptions = true,
        description = "CLI client for IBM Ada.")
public final class AdaCommand {

}
