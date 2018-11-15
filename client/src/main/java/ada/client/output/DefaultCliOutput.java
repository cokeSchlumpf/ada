package ada.client.output;

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
public class DefaultCliOutput implements CliOutput {

    private final PrintStream out;

    public static DefaultCliOutput apply() {
        return apply(System.out);
    }

    @Override
    public PrintStream getPrintStream() {
        return out;
    }

    @Override
    public void println(String message, Object... args) {
        out.println(String.format(message, args));
    }

}
