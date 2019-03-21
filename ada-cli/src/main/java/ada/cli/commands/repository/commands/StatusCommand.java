package ada.cli.commands.repository.commands;

import ada.cli.commands.StandardOptions;
import ada.cli.consoles.CommandLineConsole;
import picocli.CommandLine;

@CommandLine.Command(
    name = "status",
    description = "shows the status of the repository")
public class StatusCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private RepositoryCommand repositoryCommand;

    private StatusCommand(CommandLineConsole console) {
        this.console = console;
    }

    public static StatusCommand apply(CommandLineConsole console) {
        return new StatusCommand(console);
    }

    @Override
    public void run() {
        console.message("The status of the repository %s", repositoryCommand.getName());
    }

}
