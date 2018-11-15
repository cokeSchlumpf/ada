package ada.client;

import ada.client.output.CliOutput;
import com.google.common.collect.ImmutableList;
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
public final class CliApplication {

    private final ImmutableList<String> arguments;

    private final CommandLine cli;

    private final CliOutput out;

    public void run() {
        try {
            String[] arguments = new String[this.arguments.size()];
            arguments = this.arguments.toArray(arguments);

            cli.parseWithHandler(new RunLast(), arguments);
        } catch (ExecutionException ex) {
            CommandLine.usage(ex.getCommandLine(), this.out.getPrintStream());
        }
    }

}
