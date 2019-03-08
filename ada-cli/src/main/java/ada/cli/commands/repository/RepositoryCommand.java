package ada.cli.commands.repository;

import ada.cli.commands.StandardOptions;
import ada.cli.consoles.CommandLineConsole;
import picocli.CommandLine;

@CommandLine.Command(
    name = "repo",
    description = "work with a data repository",
    subcommands = {
        CommitCommand.class
    })
public class RepositoryCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.Parameters(index = "0", description = "The name of the repository")
    private String name;

    private RepositoryCommand(CommandLineConsole console) {
        this.console = console;
    }

    public static RepositoryCommand apply(CommandLineConsole console) {
        return new RepositoryCommand(console);
    }

    @Override
    public void run() {
        console.message("Some information about %s", name);
    }

}
