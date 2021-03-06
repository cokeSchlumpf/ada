package ada.adapters.cli.features;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.Application;
import ada.adapters.cli.commands.CommandFactory;

public class ApplicationContext {

    private final StringBuffer output;

    private final CommandLineConsole console;

    private ApplicationContext() {
        this.output = new StringBuffer();
        this.console = CommandLineConsole.apply(output);
    }

    public static ApplicationContext apply() {
        return new ApplicationContext();
    }

    public CommandLineConsole getConsole() {
        return console;
    }

    public void clearOutput() {
        System.out.print(output);
        output.delete(0, output.length());
    }

    public StringBuffer getOutput() {
        return output;
    }

    public void run(String ...args) {
        output
            .append("$ ada ")
            .append(String.join(" ", args))
            .append("\n");

        Application.apply(CommandFactory.apply(console)).run(args);
    }

}
