package ada.cli.output;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.PrintStream;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
public class DefaultOutput implements CliOutput {

    private final PrintStream out;

    @Override
    public PrintStream getPrintStream() {
        return out;
    }

    @Override
    public void println(String message, Object... args) {
        out.println(String.format(message, args));
    }

}
