package ada.adapters.cli.consoles;

import lombok.AllArgsConstructor;

import java.io.PrintStream;

@AllArgsConstructor(staticName = "apply")
final class SystemCommandLineConsole extends CommandLineConsole {

    @Override
    public PrintStream printStream() {
        return System.out;
    }

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println(String message) {
        System.out.println(message);
    }

}
