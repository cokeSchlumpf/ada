package ada.vcs.client.converters;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.google.common.base.Stopwatch;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class CSVSourceBuilderUTest {

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

        Path p = Paths.get("/Users/michael/Workspaces/notebook/Beispieldaten/sample-10000000.csv");
        CSVSourceBuilder s = CSVSourceBuilder.apply(
            p, Lists.newArrayList(), ';', '"', '\\', ActorMaterializer.create(system));

        Stopwatch sw = Stopwatch.createStarted();
        s.get(true).toCompletableFuture().get();
        sw.stop();
        System.out.println(sw);

    }

}
