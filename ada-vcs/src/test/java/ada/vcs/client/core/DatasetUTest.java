package ada.vcs.client.core;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.ResourceName;
import ada.vcs.client.converters.csv.CSVSource;
import ada.vcs.client.converters.api.ReadableDataSource;
import ada.vcs.client.converters.internal.contexts.FileContext;
import ada.vcs.client.core.dataset.Dataset;
import ada.vcs.client.core.dataset.DatasetImpl;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class DatasetUTest {

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
    public void test() throws ExecutionException, InterruptedException, IOException {
        Path p = Paths.get("/Users/michael/Workspaces/notebook/Beispieldaten/sample-10000000.csv");

        CSVSource source = CSVSource
            .builder(p)
            .build();

        ReadableDataSource<FileContext> rs = source
            .analyze(ActorMaterializer.create(system))
            .toCompletableFuture()
            .get();

        Schema schema = rs.getSchema();

        Dataset ds = DatasetImpl.apply(ResourceName.apply("foo"), source, schema);

        ObjectMapper om = ObjectMapperFactory.apply().create(true);

        String json = om.writeValueAsString(ds);
        System.out.println(json);

        Dataset dsp = om.readValue(json, DatasetImpl.class);

        System.out.println(dsp);
    }

}
