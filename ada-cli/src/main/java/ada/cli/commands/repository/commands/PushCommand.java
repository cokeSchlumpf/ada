package ada.cli.commands.repository.commands;

import ada.cli.commands.StandardOptions;
import ada.cli.consoles.CommandLineConsole;
import picocli.CommandLine;

@CommandLine.Command(
    name = "push",
    description = "push local commit to remote and load data into data-repository")
public class PushCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private RepositoryCommand repositoryCommand;

    private PushCommand(CommandLineConsole console) {
        this.console = console;
    }

    public static PushCommand apply(CommandLineConsole console) {
        return new PushCommand(console);
    }

    @Override
    public void run() {
        console.message("Pushing repository '%s'", repositoryCommand.getName());
    }

}
