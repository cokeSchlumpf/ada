package ada.cli;

import ada.server.AdaServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApplicationITest {

    private AdaServer server;

    @Before
    public void before() {
        server = AdaServer.apply();
        server.start();
    }

    @After
    public void after() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void test() {
        AdaCLI.main("repo", "foo", "commit", "hello world!");

        System.out.println("---");

        AdaCLI.main("repo", "foo", "init", "csv", "foo.csv");
    }

}
