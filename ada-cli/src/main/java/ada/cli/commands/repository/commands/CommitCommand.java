package ada.cli.commands.repository.commands;

import ada.cli.commands.StandardOptions;
import ada.cli.consoles.CommandLineConsole;
import picocli.CommandLine;

@CommandLine.Command(
    name = "commit",
    description = "commits the current working directory")
public class CommitCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private RepositoryCommand repositoryCommand;

    @CommandLine.Parameters(index = "0", description = "The commit message")
    private String message;

    private CommitCommand(CommandLineConsole console) {
        this.console = console;
    }

    public static CommitCommand apply(CommandLineConsole console) {
        return new CommitCommand(console);
    }

    @Override
    public void run() {
        console.message("Commit with message '" + message + "' of '" + repositoryCommand.getName() +"'");
    }

}
