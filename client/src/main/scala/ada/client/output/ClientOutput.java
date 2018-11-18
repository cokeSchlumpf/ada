package ada.client.output;

import java.io.PrintStream;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
public interface ClientOutput {

    PrintStream getPrintStream();

    void println(String message, Object... args);

    void println(String message);

}
