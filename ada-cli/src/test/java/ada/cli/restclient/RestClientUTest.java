package ada.cli.restclient;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.*;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class RestClientUTest {

    private static final int PORT = 8180;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Value
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "apply")
    @NoArgsConstructor(force = true,
                       access = AccessLevel.PRIVATE)
    private static class MyClass {

        private final String foo;

        private final int bar;

    }

    @Test
    public void testGet() {
        /*
         * Setup a simple wiremock stub
         */
        stubFor(get(urlEqualTo("/my/resource"))
                    .withHeader("Accept", equalTo("application/json"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader(
                                "Content-Type",
                                "application/json")
                            .withBody("{ \"foo\": \"Hello\", \"bar\": 3 }")));

        /*
         * Test client
         */
        RestClient client = RestClientImpl.apply("http://localhost:" + PORT, new ObjectMapper());

        assertThat(client.get("/my/resource", MyClass.class))
            .isNotNull()
            .matches(c -> c.foo.equals("Hello"))
            .matches(c -> c.bar == 3);
    }

    @Test
    public void testEvents() throws ExecutionException, InterruptedException {
        /*
         * Setup a simple wiremock stub
         */
        String event01 = "{ \"foo\": \"Hello\", \"bar\": 3 }";
        String event02 = "{ \"foo\": \"Bye\", \"bar\": 4 }";

        stubFor(get(urlEqualTo("/my/resource"))
                    .withHeader("Accept", equalTo("text/event-stream"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "text/event-stream")
                            .withBody("data: " + event01 + "\n\ndata: " + event02)));

        /*
         * Test client
         */
        RestClient client = RestClientImpl.apply("http://localhost:" + PORT, new ObjectMapper());

        Publisher<MyClass> publisher = client.events("/my/resource", MyClass.class);

        ActorSystem system = ActorSystem.create("test");
        ActorMaterializer mat = ActorMaterializer.create(system);

        CompletionStage<List<MyClass>> done = Source
            .fromPublisher(publisher)
            .runWith(
                Sink.fold(
                    Lists.newArrayList(),
                    (list, obj) -> {
                        list.add(obj);
                        return list;
                    }),
                mat);

        List<MyClass> result = done.toCompletableFuture().get();

        assertThat(result.get(0))
            .isNotNull()
            .matches(c -> c.foo.equals("Hello"))
            .matches(c -> c.bar == 3);

        assertThat(result.get(1))
            .isNotNull()
            .matches(c -> c.foo.equals("Bye"))
            .matches(c -> c.bar == 4);

        mat.shutdown();
        system.terminate();
    }


}
