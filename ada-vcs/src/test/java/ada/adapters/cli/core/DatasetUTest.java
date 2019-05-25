package ada.adapters.cli.core;

import ada.commons.util.ResourceName;
import ada.adapters.cli.converters.api.ReadableDataSource;
import ada.adapters.cli.converters.csv.CSVSource;
import ada.adapters.cli.core.dataset.Dataset;
import ada.adapters.cli.core.dataset.DatasetFactory;
import ada.adapters.cli.util.AbstractAdaTest;
import org.apache.avro.Schema;
import org.junit.Ignore;
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
    @Ignore
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
