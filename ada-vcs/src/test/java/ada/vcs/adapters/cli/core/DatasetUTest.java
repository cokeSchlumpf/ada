package ada.vcs.adapters.cli.core;

import ada.commons.util.ResourceName;
import ada.vcs.domain.legacy.converters.api.ReadableDataSource;
import ada.vcs.domain.legacy.converters.csv.CSVSource;
import ada.vcs.adapters.cli.core.dataset.Dataset;
import ada.vcs.adapters.cli.core.dataset.DatasetFactory;
import ada.vcs.client.util.AbstractAdaTest;
import org.apache.avro.Schema;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class DatasetUTest extends AbstractAdaTest {

    @Test
    public void test() throws ExecutionException, InterruptedException, IOException {
        Path p = Paths.get("/Users/michael/Workspaces/notebook/Beispieldaten/sample-10000000.csv");

        CSVSource source = CSVSource
            .builder(p)
            .build();

        ReadableDataSource rs = source
            .analyze(getContext().materializer())
            .toCompletableFuture()
            .get();

        Schema schema = rs.schema();

        DatasetFactory factory = getContext().factories().datasetFactory();
        Dataset ds = factory.createDataset(ResourceName.apply("foo"), source, schema);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ds.writeTo(baos);

        String json = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        System.out.println(json);

        ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes());
        Dataset dsp = factory.createDataset(is);

        System.out.println(dsp);
    }

}
