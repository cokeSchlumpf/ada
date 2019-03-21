package ada.cli.commands.repository.commands;

import ada.cli.commands.StandardOptions;
import ada.cli.consoles.CommandLineConsole;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "csv",
    description = "initialize a repository based on existing metadata")
public class InitCSVCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.Parameters(index = "0", description = "the source csv file")
    private File file;

    @CommandLine.Option(
        names = "-c",
        description = "column separator")
    private String columnSeparator = ",";

    @CommandLine.Option(
        names = "-l",
        description = "separator for lines")
    private String lineSeparator;

    @CommandLine.ParentCommand
    private InitCommand initCommand;

    private InitCSVCommand(CommandLineConsole console) {
        this.console = console;
    }

    public static InitCSVCommand apply(CommandLineConsole console) {
        return new InitCSVCommand(console);
    }

    @Override
    public void run() {
        console.message(
            "Initialize repository %s based on meta-file with column separator: %s",
            initCommand.getRepositoryCommand().getName(),
            columnSeparator);
    }

}
