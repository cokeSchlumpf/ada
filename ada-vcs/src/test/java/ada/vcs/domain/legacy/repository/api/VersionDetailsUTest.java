package ada.vcs.domain.legacy.repository.api;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import ada.vcs.domain.legacy.repository.api.version.VersionFactory;
import org.junit.Test;

public class VersionDetailsUTest {

    @Test
    public void test() {
        ObjectMapperFactory omf = ObjectMapperFactory.apply();

        VersionDetails details = VersionFactory
            .apply(omf.create())
            .createDetails(User.apply("hello", "hello@me.com"), null);

        System.out.println(details);
    }

}
