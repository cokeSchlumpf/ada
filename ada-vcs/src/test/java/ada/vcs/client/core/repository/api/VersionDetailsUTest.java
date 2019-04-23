package ada.vcs.client.core.repository.api;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.client.core.repository.api.version.VersionFactory;
import org.junit.Test;

public class VersionDetailsUTest {

    @Test
    public void test() {
        ObjectMapperFactory omf = ObjectMapperFactory.apply();

        VersionDetails details = VersionFactory
            .apply(omf.create())
            .createDetails(User.apply("hello", "hello@me.com"), "Some message", null);

        System.out.println(details);
    }

}
