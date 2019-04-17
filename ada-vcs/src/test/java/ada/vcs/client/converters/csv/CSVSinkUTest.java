package ada.vcs.client.converters.csv;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.client.converters.api.DataSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CSVSinkUTest {

    @Test
    public void jsonTest() throws IOException {
        ObjectMapper om = ObjectMapperFactory.apply().create(true);
        CSVSink s = CSVSink.apply(Paths.get("foo.csv"));

        String json = om.writeValueAsString(s);
        System.out.println(json);

        DataSink sp = om.readValue(json, DataSink.class);

        assertThat(sp).isEqualTo(s);
    }

}
