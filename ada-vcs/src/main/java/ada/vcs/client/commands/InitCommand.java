package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "init",
    description = "initializes repository metadata in the current repository")
@AllArgsConstructor(staticName = "apply")
public final class InitCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @Override
    public void run() {
        console.message("Initialized empty data repository in %s/", new File(".avcs").getAbsolutePath());
    }

}
