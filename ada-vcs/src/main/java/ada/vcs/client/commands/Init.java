package ada.vcs.client.commands;

import ada.vcs.client.core.project.AdaProjectTemp;
import ada.vcs.client.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "init",
    description = "initializes Ada metadata in the current directory")
@AllArgsConstructor(staticName = "apply")
public final class Init extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.Option(
        names = {"-d", "--dir"},
        description = "the project root directory")
    private File dir;

    public static Init apply(CommandLineConsole console) {
        return apply(console, null);
    }

    @Override
    public void run() {
        if (dir == null) dir = new File(System.getProperty("user.dir"));
        AdaProjectTemp root = AdaProjectTemp.init(dir.toPath());
        console.message("Initialized ada project in '%s'.", root.getPath());
    }

}
