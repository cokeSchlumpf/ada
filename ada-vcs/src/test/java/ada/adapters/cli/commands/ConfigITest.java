package ada.adapters.cli.commands;

import ada.client.features.ApplicationContext;
import ada.client.util.AbstractAdaTest;
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
