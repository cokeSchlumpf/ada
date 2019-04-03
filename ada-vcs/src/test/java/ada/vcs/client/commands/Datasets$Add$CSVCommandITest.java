package ada.vcs.client.commands;

import ada.vcs.client.features.ApplicationContext;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Datasets$Add$CSVCommandITest {

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
        ApplicationContext context = new ApplicationContext();
        context.run("init");
        context.run("add", "csv", "/Users/michael/Workspaces/notebook/Beispieldaten/sample-10000000.csv", "fooo", "-f", ";", "-a", "100", "--verbose");
        System.out.println(context.getOutput().toString());
        System.out.println("...");
    }

    @Test
    public void fs() {
        System.out.println(new File(".").getAbsolutePath());
        System.out.println(System.getProperty("user.dir"));
        System.setProperty("user.dir", "/Users/michael/Workspaces/notebook/Beispieldaten");
        System.out.println(new File(".").getAbsolutePath());
        System.out.println(Paths.get(".").toAbsolutePath());
    }

}
