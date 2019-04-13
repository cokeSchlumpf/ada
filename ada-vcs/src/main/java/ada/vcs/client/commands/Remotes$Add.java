package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.core.remotes.FileSystemRemote;
import ada.vcs.client.core.remotes.HttpRemote;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CommandLine.Command(
    name = "add",
    description = "adds a new remote")
public final class Remotes$Add extends StandardOptions implements ProjectCommand {

    private final CommandLineConsole console;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "URL",
        description = "the URL of the remote")
    private URL url;

    @CommandLine.Parameters(
        index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the remote")
    private String alias;

    private Remotes$Add(CommandLineConsole console) {
        this.console = console;
    }

    public static Remotes$Add apply(CommandLineConsole console) {
        return new Remotes$Add(console);
    }

    @Override
    public void run(AdaProject project) {
        switch (url.getProtocol()) {
            case "http":
            case "https":
                if (alias == null || alias.trim().length() == 0) {
                    alias = Paths.get(url.toString()).getFileName().toString();
                }

                project.addRemote(HttpRemote.apply(ResourceName.apply(alias), url));
                break;
            case "file":
                Path path = Paths.get(url.toString().substring("file:".length()));

                if (!path.isAbsolute()) {
                    path = Paths
                        .get(System.getProperty("user.dir"))
                        .resolve(path)
                        .normalize();
                }

                if (!Files.exists(path)) {
                    console.message("Path '%s' does not exist or is no directory.", path.toString());
                    return;
                }

                if (alias == null || alias.trim().length() == 0) {
                    alias = path.getFileName().toString();
                }

                project.addRemote(
                    FileSystemRemote
                        .apply(ResourceName.apply(alias), path)
                        .relativize(project.getPath()));

                break;
            default:
                console.message("Unknown type");
                return;
        }

        console.message("Added new remote '%s'.", alias);
    }

}
