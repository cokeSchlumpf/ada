package ada.domain.legacy.converters.local;

import ada.domain.legacy.converters.api.WriteSummary;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class LocalSinkUTest {

    ActorSystem system;

    Materializer materializer;

    Path path;

    @Before
    public void before() {
        this.system = ActorSystem.create();
        this.materializer = ActorMaterializer.create(system);
        this.path = Files.newTemporaryFolder().toPath();
    }

    @After
    public void after() {
        if (this.system != null) {
            this.system.terminate();
            this.system = null;
        }

        try {
            Files.delete(path.toFile());
        } catch (Exception ignore) {

        }
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        File path = Files.newTemporaryFolder();

        Schema schema = SchemaBuilder
            .builder()
            .record("test")
            .fields()
            .requiredString("a")
            .requiredString("b")
            .endRecord();


        Sink<GenericRecord, CompletionStage<WriteSummary>> sink = LocalSink
            .apply(path.toPath())
            .sink(schema);

        WriteSummary writeSummary = Source
            .range(0, 10)
            .map(i -> {
                GenericRecordBuilder r = new GenericRecordBuilder(schema);
                r.set("a", "Hello");
                r.set("a", "World " + i);
                return (GenericRecord) r.build();
            })
            .toMat(sink, Keep.right())
            .run(materializer)
            .toCompletableFuture()
            .get();

        System.out.println(writeSummary);
    }

}
