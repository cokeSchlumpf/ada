package ada.cli.commands.repository.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.common.ObjectMapperFactory;
import com.ibm.ada.model.HttpEndpoint;
import com.ibm.ada.model.RelativePath;
import com.ibm.ada.model.ResourceName;
import com.ibm.ada.model.repository.Commit;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LocalRepositoryUTest {

    @Test
    public void test() throws IOException {
        ObjectMapper om = ObjectMapperFactory.apply().create();
        LocalRepository r = LocalRepository
            .apply(ResourceName.apply("foo"), RelativePath.apply("foo"))
            .withCommit(Commit.apply("abcd", "foo bar", new Date(), "egon"))
            .withRemote(Remote.apply(ResourceName.apply("bar"), HttpEndpoint.apply(new URL("http://foo.de"))));

        String json = om.writeValueAsString(r);
        assertThat(om.readValue(json, LocalRepository.class)).isEqualTo(r);
    }

}
