package ada.cli;

import ada.cli.commands.AboutCommand;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import picocli.CommandLine;

import static picocli.CommandLine.*;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
@CommandLine.Command(
    name = "ada",
    mixinStandardHelpOptions = true,
    description = "CLI client for IBM Ada.")
public class CliApplication {

    private final AboutCommand about;

    public void main(String... args) {
        CommandLine c = new CommandLine(this)
            .addSubcommand("about", about);

        try {
            c.parseWithHandler(new RunLast(), args);
        } catch (ExecutionException ex) {
            CommandLine.usage(
                ex.getCommandLine(),
                System.out);
        }

    }

}
