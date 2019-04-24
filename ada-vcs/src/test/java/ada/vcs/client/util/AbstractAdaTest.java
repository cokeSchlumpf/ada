package ada.vcs.client.util;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.features.ApplicationContext;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;

public class AbstractAdaTest {

    private ApplicationContext application;

    private CommandContext context;

    private Path directory;

    @Before
    public void before() {
        application = ApplicationContext.apply();
        context = CommandContext.apply();
        directory = Files.newTemporaryFolder().toPath();

        System.setProperty("user.dir", directory.toAbsolutePath().toString());
    }

    @After
    public void after() {
        if (context != null) {
            context.shutdown();
        }

        if (directory != null) {
            Files.delete(directory.toFile());
            directory = null;
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
}
