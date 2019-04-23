package ada.vcs.client.core.project;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AdaProjectConfigurationUTest {

    @Test
    public void test() throws IOException {
        Process exec = Runtime.getRuntime().exec("git config user.name");

        String out = IOUtils.toString(exec.getInputStream(), StandardCharsets.UTF_8);
        String error = IOUtils.toString(exec.getErrorStream(), StandardCharsets.UTF_8);
        System.out.println(out);
        System.out.println(error);
    }

}
