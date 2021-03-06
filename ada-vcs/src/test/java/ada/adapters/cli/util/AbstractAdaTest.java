package ada.adapters.cli.util;

import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.features.ApplicationContext;
import ada.commons.util.Operators;
import ada.adapters.server.Server;
import ada.adapters.server.ServerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

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
        Server server = ServerFactory.apply(context).create(serverDirectory);

        CompletableFuture.runAsync(() -> {
            try {
                server.startServer("0.0.0.0", 8080, context.system());
            } catch (Exception e) {
                ExceptionUtils.wrapAndThrow(e);
            }
        });

        Operators.suppressExceptions(() -> Thread.sleep(3000));

        return "0.0.0.0:8080";
    }

}
