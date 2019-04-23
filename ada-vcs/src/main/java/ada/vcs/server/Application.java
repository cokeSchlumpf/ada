package ada.vcs.server;

import java.util.concurrent.ExecutionException;

public final class Application {

    private Application() {

    }

    public static void main(String... args) throws ExecutionException, InterruptedException {
        final Server myServer = new Server();
        myServer.startServer("localhost", 8080);
    }

}
