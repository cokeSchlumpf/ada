package ada.vcs.client.converters.csv;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.client.converters.api.DataSink;
import ada.vcs.client.converters.api.DataSinkFactory;
import ada.vcs.client.converters.api.DataSinkMemento;
import ada.vcs.client.util.WritableUtil;
import akka.stream.javadsl.Sink;
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
