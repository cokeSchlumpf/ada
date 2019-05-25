package ada.client;

import ada.client.features.ApplicationContext;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

public class UseCaseTests {

    private Path dir;

    @Before
    public void setup() {
        dir = Files.newTemporaryFolder().toPath();
        System.setProperty("user.dir", dir.toAbsolutePath().toString());
    }

    @After
    public void cleanup() {
        if (dir != null) {
            Files.delete(dir.toFile());
            dir = null;
        }
    }

    @Test
    public void test() {
        ApplicationContext context = ApplicationContext.apply();
        context.run("init");
        context.run("dataset", "foo", "targets");
        System.out.println(context.getOutput());
    }

}
