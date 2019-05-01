package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
    name = "server",
    hidden = true,
    description = "Starts the server")
@AllArgsConstructor(staticName = "apply")
public final class Server extends StandardOptions implements Runnable {

    private final CommandContext context;

    @CommandLine.Parameters(
        index = "0",
        arity = "0..1",
        defaultValue = ".",
        paramLabel = "DIRECTORY",
        description = "the servers data root directory")
    private File dir = null;

    @CommandLine.Option(
        names = {"-i", "--hostname"},
        defaultValue = "0.0.0.0",
        description = "the servers hostname")
    private String hostname;

    @CommandLine.Option(
        names = {"-p", "--port"},
        defaultValue = "8080",
        description = "the servers port")
    private int port;

    public static Server apply(CommandContext context) {
        return apply(context, new File("."), "0.0.0.0", 8080);
    }

    @Override
    public void run() {
        final ada.vcs.server.Server myServer = ada.vcs.server.Server
            .apply(context.factories().serverDirectivesFactory().create(dir.toPath()));

        try {
            myServer.startServer(hostname, port, context.system());
        } catch (Exception e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

}
