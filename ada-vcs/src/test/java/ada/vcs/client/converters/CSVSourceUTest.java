package ada.vcs.client.converters;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.csv.CSVSink;
import ada.vcs.client.converters.csv.CSVSource;
import ada.vcs.client.converters.internal.api.ReadSummary;
import ada.vcs.client.converters.internal.api.ReadableDataSource;
import ada.vcs.client.converters.internal.contexts.FileContext;
import ada.vcs.client.converters.internal.monitors.NoOpMonitor;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import org.apache.avro.Schema;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CSVSourceUTest {

    private ActorSystem system;

    @Before
    public void before() {
        system = ActorSystem.apply("test");
    }

    @After
    public void after() {
        if (system != null) {
            system.terminate();
            system = null;
        }
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Stopwatch sw = Stopwatch.createStarted();
        ActorMaterializer materializer = ActorMaterializer.create(system);
        Path p = Paths.get("/Users/michael/Workspaces/notebook/Beispieldaten/sample-10000000.csv");

        ReadableDataSource<FileContext> s = CSVSource
            .builder(p)
            .build()
            .analyze(materializer)
            .toCompletableFuture()
            .get();


        Schema schema = s.getSchema();

        CSVSink sink = CSVSink.apply(CommandLineConsole.apply());

        ReadSummary result = s
            .getRecords(NoOpMonitor.apply())
            .map(f -> f)
            .take(100)
            .toMat(sink.sink(schema), (summary, done) -> {
                done.thenAccept(sum -> {
                    System.out.println("Write Summary: " + sum);
                });
                return summary;
            })
            .run(materializer)
            .toCompletableFuture()
            .get();

        sw.stop();
        System.out.println(result);
        System.out.println(sw);
        System.out.println(schema.toString(true));
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

        CSVSource s2 = ObjectMapperFactory
            .apply()
            .create()
            .readValue(json, CSVSource.class);

        assertThat(s2.getCommentChar()).isEqualTo('#');
        System.out.println(om.writeValueAsString(source));
    }

}
