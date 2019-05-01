package ada.vcs.client.util;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.features.ApplicationContext;
import ada.vcs.server.Server;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AbstractAdaTest {

    private ApplicationContext application;

    private CommandContext context;

    private Path directory;

    private Path serverDirectory;

    @Before
    public void before() throws Exception {
        application = ApplicationContext.apply();
        context = CommandContext.apply();
        directory = Files.newTemporaryFolder().toPath();
        serverDirectory = Files.newTemporaryFolder().toPath();

        System.setProperty("user.dir", directory.toAbsolutePath().toString());
    }

    @After
    public void after() throws Exception {
        if (context != null) {
            context.shutdown();
        }

        if (directory != null) {
            Files.delete(directory.toFile());
            directory = null;
        }

        if (serverDirectory != null) {
            Files.delete(serverDirectory.toFile());
        }
    }

    public ApplicationContext getApplication() {
        return application;
    }

    public CommandContext getContext() {
        return context;
    }

    public Path getDirectory() {
        return directory;
    }

    protected String getServer() {
        Server server = Server.apply(context.factories().serverDirectivesFactory().create(serverDirectory));

        CompletableFuture.runAsync(() -> {
            try {
                server.startServer("0.0.0.0", 8080, context.system());
            } catch (Exception e) {
                ExceptionUtils.wrapAndThrow(e);
            }
        });

        return "localhost:8080";
    }

}
