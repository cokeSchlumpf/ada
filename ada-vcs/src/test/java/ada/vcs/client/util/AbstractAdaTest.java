package ada.vcs.client.util;

import ada.vcs.client.commands.context.CommandContext;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;

public class AbstractAdaTest {

    private CommandContext context;

    private Path directory;

    @Before
    public void before() {
        context = CommandContext.apply();
        directory = Files.newTemporaryFolder().toPath();
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

    public CommandContext getContext() {
        return context;
    }

    public Path getDirectory() {
        return directory;
    }
}
