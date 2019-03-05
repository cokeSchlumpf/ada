package ada.cli.consoles;

import org.junit.Test;

import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandLineConsoleUTest {

    @Test
    public void testPrintWriter() {
        StringBuffer sb = new StringBuffer();
        CommandLineConsole c = CommandLineConsole.apply(sb);
        PrintStream ps = c.printStream();

        ps.println("Hello World!");
        ps.print(42);

        assertThat(sb.toString())
            .startsWith("Hello World!")
            .endsWith("42")
            .contains(System.lineSeparator());
    }

}
