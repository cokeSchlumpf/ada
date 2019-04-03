package ada.vcs.client.commands;

import ada.vcs.client.features.ApplicationContext;
import org.junit.Test;

public class InitITest {

    @Test
    public void test() {
        ApplicationContext context = new ApplicationContext();
        context.run("init", "-d", "/Users/michael/Workspaces/notebook/Beispieldaten/test", "--verbose");
        System.out.println(context.getOutput());
    }

}
