package ada.domain.legacy.repository.api;

import ada.commons.databind.ObjectMapperFactory;
import ada.domain.dvc.values.repository.User;
import ada.domain.dvc.values.repository.version.VersionDetails;
import ada.domain.dvc.values.repository.version.VersionFactory;
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
