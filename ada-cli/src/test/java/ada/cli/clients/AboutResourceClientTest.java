package ada.cli.clients;

import ada.cli.configuration.ApplicationConfiguration;
import ada.cli.testutil.AdaTestRootConfiguration;
import ada.client.output.Output;
import ada.client.output.StringOutput;
import ada.web.api.resources.about.model.AnonymousUser;
import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.User;
import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletionStage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { //
    AdaTestRootConfiguration.class, //
    AboutResourceClientTest.TestAdaConfiguration.class}
)
public class AboutResourceClientTest {


    private static final int PORT = 8190;

    @Configuration
    public static class TestAdaConfiguration {

        @Bean
        @Primary
        public Output getOtput(ApplicationConfiguration config, Materializer materializer) {
            return StringOutput.apply();
        }

        @Bean
        public ApplicationConfiguration applicationConfiguration() {
            ApplicationConfiguration applConfig = new ApplicationConfiguration();
            applConfig.setServer(new ApplicationConfiguration.Server());
            applConfig.getServer().setBaseUrl("http://localhost:" + PORT);
            return applConfig;
        }
    }


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Autowired
    private AboutResourceClient aboutResource = null;

    @Autowired
    private Materializer materializer = null;

    @Autowired
    private Output output = null;


    @After
    public void tearDown() {
        wireMockRule.resetAll();
    }


    @Test
    public void testGetAboutUser() {

        prepareGetUserStub();

        User user = aboutResource.getUser(AnonymousUser.apply());
        Assert.assertTrue(user.getRoles().contains("role1"));
    }

    @Test
    public void testGetAbout() {

        prepareGetAboutStub();

        Application application = aboutResource.getApplicationAsObject();

        Assert.assertNotNull(application);
        Assert.assertNotNull(application.build);
        Assert.assertNotNull(application.name);
    }

    @Test
    public void testStreamAboutUser() throws Exception {
        prepareStreamAboutUserStub();

        Publisher<String> userAsStream = aboutResource.getUserAsStream(AnonymousUser.apply());

        CompletionStage<Done> done = Source
            .fromPublisher(userAsStream)
            .runWith(Sink.foreach(output::message), materializer);

        done.toCompletableFuture().get();

        Assert.assertTrue(output.toString().contains("user"));
        Assert.assertTrue(output.toString().contains("expert"));
    }

    @Test
    public void testStreamAbout() throws Exception {
        prepareStreamAboutStub();

        Publisher<String> applicationAsStream = aboutResource.getApplicationAsStream();

        CompletionStage<Done> done = Source
            .fromPublisher(applicationAsStream)
            .runWith(Sink.foreach(output::message), materializer);

        done.toCompletableFuture().get();

        Assert.assertTrue(output.toString().contains("bla"));
        Assert.assertTrue(output.toString().contains("blu"));
    }

    private void prepareGetUserStub() {
        String user = "{\"class\":\"ada.web.api.resources.about.model.Anonymous\",\"roles\":[\"role1\"]}";

        stubFor(get(urlEqualTo("/api/v1/about/user"))
            .withHeader(
                "Accept",
                equalTo("application/json"))
            .willReturn(aResponse().withStatus(200)
                .withHeader(
                    "Content-Type",
                    "application/json")
                .withBody(user)));

    }

    private void prepareGetAboutStub() {
        String user = "{\"name\":\"ada-server (local)\",\"build\":\"0.0.43\"}";

        stubFor(get(urlEqualTo("/api/v1/about"))
            .withHeader(
                "Accept",
                equalTo("application/json"))
            .willReturn(aResponse().withStatus(200)
                .withHeader(
                    "Content-Type",
                    "application/json")
                .withBody(user)));
    }

    private void prepareStreamAboutStub() {

        stubFor(get(urlEqualTo("/api/v1/about"))
            .withHeader(
                "Accept",
                equalTo("text/event-stream"))
            .willReturn(aResponse().withStatus(200)
                .withHeader(
                    "Content-Type",
                    "text/event-stream")
                .withBody("data:bla bla bla\n\ndata:blu blu blu")));
    }

    private void prepareStreamAboutUserStub() {
        stubFor(get(urlEqualTo("/api/v1/about/user"))
            .withHeader(
                "Accept",
                equalTo("text/event-stream"))
            .willReturn(aResponse().withStatus(200)
                .withHeader(
                    "Content-Type",
                    "text/event-stream")
                .withBody("data:dear user\n\ndata:you are an expert")));

    }

}
