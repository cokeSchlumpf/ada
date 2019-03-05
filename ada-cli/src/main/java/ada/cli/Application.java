package ada.cli;

import ada.cli.consoles.CommandLineConsole;

public class Application {

    public static void main(String... args) {
        CommandLineConsole console = CommandLineConsole.apply();
        CommandFactory commands = CommandFactory.apply(console);
        CommandLineRunner runner = CommandLineRunner.apply(commands);

        runner.run(args);
    }

}
