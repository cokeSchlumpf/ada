package ada.vcs.client.features;

import ada.vcs.client.Application;
import ada.vcs.client.commands.CommandFactory;
import ada.vcs.client.consoles.CommandLineConsole;

public class ApplicationContext {

    private final StringBuffer output;

    private final CommandLineConsole console;

    public ApplicationContext() {
        this.output = new StringBuffer();
        this.console = CommandLineConsole.apply(output);
    }

    public CommandLineConsole getConsole() {
        return console;
    }

    public void clearOutput() {
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
