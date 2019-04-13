package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.project.AdaProjectTemp;
import ada.vcs.client.exceptions.NoProjectException;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "local",
    description = "defines a local repository as target")
public final class Dataset$Targets$Add$Local extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private Dataset$Targets$Add add;

    @CommandLine.Parameters(index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the target")
    private String alias;

    @CommandLine.Parameters(index = "0",
        paramLabel = "DIRECTORY",
        description = "the location of the local repository")
    private File directory;

    private Dataset$Targets$Add$Local(CommandLineConsole console) {
        this.console = console;
    }

    public static Dataset$Targets$Add$Local apply(CommandLineConsole console) {
        return new Dataset$Targets$Add$Local(console);
    }

    @Override
    public void run() {
        AdaProjectTemp project = AdaProjectTemp.fromHere().orElseThrow(NoProjectException::apply);

        if (alias == null) {
            alias = FilenameUtils.removeExtension(directory.getName());
        }

        console.message("TODO: Add local target");
    }

}
