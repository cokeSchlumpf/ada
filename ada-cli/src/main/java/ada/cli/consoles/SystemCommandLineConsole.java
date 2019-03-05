package ada.cli.consoles;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
final class SystemCommandLineConsole extends CommandLineConsole {

    @Override
    protected void print(String message) {
        System.out.print(message);
    }

    @Override
    protected void println(String message) {
        System.out.println(message);
    }

}
