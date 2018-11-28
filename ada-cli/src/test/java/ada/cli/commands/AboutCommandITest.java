package ada.cli.commands;

import ada.cli.configuration.ApplicationConfiguration;
import ada.cli.configuration.picocli.ApplicationFactory;
import ada.cli.testutil.AdaTestCommandLineRunner;
import ada.client.output.Output;
import ada.client.output.StringOutput;
import akka.stream.Materializer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class AboutCommandITest {

    private static final int PORT = 8080;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Autowired
    private Output output = null;

    @Autowired
    private CommandLineRunner adaCommandLineRunner = null;

    @TestConfiguration
    public static class TestAdaConfiguration {

        @Bean
        @Primary
        public Output getOutput(ApplicationConfiguration config, Materializer materializer) {
            return StringOutput.apply();
        }

        @Bean
        @Primary
        public CommandLineRunner adaTestCommandLineRunner(ApplicationFactory factory, Output output) {
            return new AdaTestCommandLineRunner(factory, output);
        }
    }


    private static final String event01 = "event1";
    private static final String event02 = "event2";

    @After
    public void tearDown() {
        wireMockRule.resetAll();
    }


    @Test
    public void testGetAboutStream() throws Exception {

        prepareAboutStub();

        runAndCheck( //
                     new String[]{"about"},
                     new String[]{event01, event02});
    }

    @Test
    public void testGetAboutStreamWithUserStream() throws Exception {

        prepareAboutStub();
        prepareUserStub();

        runAndCheck( //
                     new String[]{"about", "-u"},
                     new String[]{event01, event02, "Ada Command Line Interface", "expert", "authenticated"});
    }

    @Test
    public void testGetAboutStreamWithUser2Stream() throws Exception {

        prepareAboutStub();
        prepareUserStub();

        runAndCheck( //
                     new String[]{"about", "--user"},
                     new String[]{event01, event02, "Ada Command Line Interface", "expert", "authenticated"});
    }


    @Test
    public void testGetAboutStreamWithClientInfo() throws Exception {
        prepareAboutStub();

        runAndCheck( //
                     new String[]{"about", "-c"},
                     new String[]{event01, event02, "Ada Command Line Interface"});
    }


    @Test
    public void testGetAboutStreamWithClient2Info() throws Exception {
        prepareAboutStub();

        runAndCheck( //
                     new String[]{"about", "--cli"},
                     new String[]{event01, event02, "Ada Command Line Interface"});
    }

    @Test
    public void testGetAboutStreamWithClient3Info() throws Exception {
        prepareAboutStub();

        runAndCheck( //
                     new String[]{"about", "--client"},
                     new String[]{event01, event02, "Ada Command Line Interface"});
    }

    @Test
    public void testAboutWithVersionArgument() throws Exception {

        runAndCheck( //
                     new String[]{"about", "-V"},
                     new String[]{"Ada Command Line Interface"});
    }

    @Test
    public void testAboutWithVersion2Argument() throws Exception {

        runAndCheck( //
                     new String[]{"about", "--version"},
                     new String[]{"Ada Command Line Interface"});
    }

    @Test
    public void testAboutWrongArgument() throws Exception {

        runAndCheck( //
                     new String[]{"about", "xy"},
                     new String[]{"Unmatched argument: xy"});
    }

    private void runAndCheck(String[] commandsArgs, String[] stringsToCheck) throws Exception {
        adaCommandLineRunner.run(commandsArgs);

        System.out.println(output.toString());

        Stream.of(stringsToCheck).forEach(s -> assertThat(output.toString()).contains(s));
    }

    private void prepareAboutStub() {

        stubFor(get(urlEqualTo("/api/v1/about"))
                    .withHeader(
                        "Accept",
                        equalTo("text/event-stream"))
                    .willReturn(aResponse().withStatus(200)
                                           .withHeader(
                                               "Content-Type",
                                               "text/event-stream")
                                           .withBody("data: " + event01 + "\n\ndata: " + event02)));
    }

    private void prepareUserStub() {
        String userMessage = "data:You are authenticated as TheUser\n\n"
                             + "data:  -  \n\n"
                             + "data:You have assigned the following roles:\n\n"
                             + "data:expert";

        stubFor(get(urlEqualTo("/api/v1/about/user"))
                    .withHeader(
                        "Accept",
                        equalTo("text/event-stream"))
                    .willReturn(aResponse().withStatus(200)
                                           .withHeader(
                                               "Content-Type",
                                               "text/event-stream")
                                           .withBody(userMessage)));
    }

}
