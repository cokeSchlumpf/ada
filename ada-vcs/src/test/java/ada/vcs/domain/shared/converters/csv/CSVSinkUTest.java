package ada.vcs.domain.shared.converters.csv;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.domain.shared.converters.api.DataSink;
import ada.vcs.domain.shared.converters.api.DataSinkFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CSVSinkUTest {

    @Test
    public void jsonTest() throws IOException {
        ObjectMapper om = ObjectMapperFactory.apply().create(true);
        DataSinkFactory factory = DataSinkFactory.apply();
        CSVSink s = CSVSink.apply(Paths.get("foo.csv"));

        String json = om.writeValueAsString(s.memento());
        System.out.println(json);

        CSVSinkMemento sp = om.readValue(json, CSVSinkMemento.class);
        DataSink sinkParsed = factory.createDataSink(sp);


        assertThat(sinkParsed).isEqualTo(s);
    }

}
