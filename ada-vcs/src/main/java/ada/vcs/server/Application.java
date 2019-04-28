package ada.vcs.server;

import ada.vcs.client.commands.context.CommandContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public final class Application {

    private Application() {

    }

    public static void main(String... args) throws ExecutionException, InterruptedException {
        final Path data = Paths.get("/Users/michael/Downloads/akka-http-tests");
        final CommandContext context = CommandContext.apply();

        final Server myServer = Server
            .apply(context.factories().serverDirectivesFactory().create(data));

        myServer.startServer("localhost", 8080, context.system());
    }

}
