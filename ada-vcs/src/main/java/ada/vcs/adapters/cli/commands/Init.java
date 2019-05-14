package ada.vcs.adapters.cli.commands;

import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.core.project.AdaProject;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "init",
    description = "initializes Ada metadata in the current directory")
@AllArgsConstructor(staticName = "apply")
public final class Init extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Option(
        names = {"-d", "--dir"},
        description = "the project root directory")
    private File dir = null;

    public static Init apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        if (dir == null) dir = new File(System.getProperty("user.dir"));
        AdaProject root = context.factories().projectFactory().init(dir.toPath());
        console.message("Initialized ada project in '%s'.", root.path());
    }

}
