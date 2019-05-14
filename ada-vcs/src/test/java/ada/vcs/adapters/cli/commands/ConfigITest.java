package ada.vcs.adapters.cli.commands;

import ada.vcs.client.features.ApplicationContext;
import ada.vcs.client.util.AbstractAdaTest;
import org.junit.Test;

public class ConfigITest extends AbstractAdaTest {

    @Test
    public void test() {
        ApplicationContext app = getApplication();

        // Given an initialized project
        app.run("init");

        app.run("config");

        System.out.println(app.getOutput());
    }

}
