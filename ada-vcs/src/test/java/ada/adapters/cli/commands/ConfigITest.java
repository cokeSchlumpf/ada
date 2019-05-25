package ada.adapters.cli.commands;

import ada.adapters.cli.features.ApplicationContext;
import ada.adapters.cli.util.AbstractAdaTest;
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
