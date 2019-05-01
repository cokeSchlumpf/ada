package ada.vcs.client.converters.csv;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.FileSize;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.api.ReadSummary;
import ada.vcs.client.converters.api.ReadableDataSource;
import ada.vcs.client.converters.internal.monitors.NoOpMonitor;
import ada.vcs.client.util.AbstractAdaTest;
import ada.vcs.client.util.TestDataFactory;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import org.apache.avro.Schema;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CSVSourceUTest extends AbstractAdaTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Materializer materializer = getContext().materializer();
        Path p = TestDataFactory.createSampleCSVFile(getDirectory(), "foo.csv", FileSize.apply(500, FileSize.Unit.MEGABYTES));

        ReadableDataSource s = CSVSource
            .builder(p)
            .build()
            .analyze(materializer)
            .toCompletableFuture()
            .get();


        Schema schema = s.schema();

        // CSVSink sink = CSVSink.apply(CommandLineConsole.apply());

        Stopwatch sw = Stopwatch.createStarted();
        ReadSummary result = s
            .getRecords(NoOpMonitor.apply())
            .toMat(Sink.ignore(), (summary, done) -> {
                /*
                done.thenAccept(sum -> {
                    System.out.println("Write Summary: " + sum);
                });
                */
                return summary;
            })
            .run(materializer)
            .toCompletableFuture()
            .get();

        sw.stop();
        System.out.println(result);
        System.out.println(sw);

        // System.out.println(schema.toString(true));
    }

    @Test
    public void json() throws IOException {
        ObjectMapper om = ObjectMapperFactory.apply().create(true);
        CSVSource source = CSVSource
            .builder(Paths.get("foo"))
            .headers(Lists.newArrayList("bla", "bla", "lorem"))
            .build();

        String json = "{\n" +
            "  \"type\" : \"csv\"," +
            "  \"file\" : \"file:///Users/michael/Workspaces/ada/ada-vcs/foo\",\n" +
            "  \"headers\" : [ \"bla\", \"bla\", \"lorem\" ],\n" +
            "  \"records-analyzed\" : 1000\n" +
            "}";

        CSVSourceMemento s2 = ObjectMapperFactory
            .apply()
            .create()
            .readValue(json, CSVSourceMemento.class);

        CSVSource parsed = CSVSource.apply(s2);

        assertThat(parsed.getCommentChar()).isEqualTo('#');
        System.out.println(om.writeValueAsString(source));
    }

}
