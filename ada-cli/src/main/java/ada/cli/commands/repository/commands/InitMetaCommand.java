package ada.cli.commands.repository.commands;

import ada.cli.commands.StandardOptions;
import ada.cli.consoles.CommandLineConsole;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "meta",
    description = "initialize a repository based on existing metadata")
public class InitMetaCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.Parameters(index = "0", description = "the metadata file")
    private File file;

    @CommandLine.ParentCommand
    private InitCommand initCommand;

    private InitMetaCommand(CommandLineConsole console) {
        this.console = console;
    }

    public static InitMetaCommand apply(CommandLineConsole console) {
        return new InitMetaCommand(console);
    }

    @Override
    public void run() {
        console.message("Initialize repository based on meta-file");
    }

}
