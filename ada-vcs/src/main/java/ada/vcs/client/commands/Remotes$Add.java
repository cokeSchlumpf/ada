package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CommandLine.Command(
    name = "add",
    description = "adds a new remote")
public final class Remotes$Add extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "URL",
        description = "the URL of the remote")
    private URL url = null;

    @CommandLine.Parameters(
        index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the remote")
    private String alias = null;

    private Remotes$Add(CommandLineConsole console, CommandContext context) {
        this.console = console;
        this.context = context;
    }

    public static Remotes$Add apply(CommandLineConsole console, CommandContext context) {
        return new Remotes$Add(console, context);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            switch (url.getProtocol()) {
                case "http":
                case "https":
                    if (alias == null || alias.trim().length() == 0) {
                        alias = Paths.get(url.toString()).getFileName().toString();
                    }

                    project.addRemote(context
                        .factories()
                        .remotesFactory()
                        .createHttpRemote(ResourceName.apply(alias), url));
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
                        context
                            .factories()
                            .remotesFactory()
                            .createFileSystemRemote(ResourceName.apply(alias), path));

                    break;
                default:
                    console.message("Unknown type");
                    return;
            }

            console.message("Added new remote '%s'.", alias);
        });
    }

}
