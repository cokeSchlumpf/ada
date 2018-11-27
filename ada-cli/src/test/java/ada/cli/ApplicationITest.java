package ada.cli;

import org.junit.Test;

public class ApplicationITest {

    // TODO: Add test for wrong command
    // TODO: Add test for unused AboutResourceClient methods
    // TODO: Test for user und config
    // TODO: Test for equals und hashcode Parameterized Type


    @Test
    public void test() {
        Application.main("about", "-u", "-c");
    }


}
