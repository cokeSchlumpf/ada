package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "local",
    description = "defines a local repository as target")
public final class Dataset$Targets$Add$Local extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset$Targets$Add add = null;

    @CommandLine.Parameters(index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the target")
    private String alias = null;

    @CommandLine.Parameters(index = "0",
        paramLabel = "DIRECTORY",
        description = "the location of the local repository")
    private File directory = null;

    private Dataset$Targets$Add$Local(CommandLineConsole console, CommandContext context) {
        this.console = console;
        this.context = context;
    }

    public static Dataset$Targets$Add$Local apply(CommandLineConsole console, CommandContext context) {
        return new Dataset$Targets$Add$Local(console, context);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            if (alias == null) {
                alias = FilenameUtils.removeExtension(directory.getName());
            }

            console.message("TODO: Add local target");
        });
    }

}
