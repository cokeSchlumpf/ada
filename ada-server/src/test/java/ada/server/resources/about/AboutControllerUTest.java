package ada.server.resources.about;

import ada.server.configuration.ApplicationConfiguration;
import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.AnonymousUser;
import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.AuthenticatedUser;
import ada.web.api.resources.about.model.User;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.Sink;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AboutControllerUTest {

    // Must be equal to what is set in /src/main/resources/application.yml
    private final static String name = "ada-server";

    // Must be equal to what is set in /src/main/resources/application.yml
    private final static String build = "0.0.42";

    private final static String username = "egon olsen";

    private final static String role = "admin";

    @Autowired
    private TestRestTemplate restTemplate = null;

    @LocalServerPort
    private int port = 0;

    @TestConfiguration
    public static class TestPythagorasConfiguration {

        @Bean
        @Primary
        public AboutResource aboutResource(ApplicationConfiguration config, Materializer materializer) {
            final Application application = Application.apply(config.getName(),config.getBuild());

            final AuthenticatedUser user = AuthenticatedUser.apply(username, ImmutableSet.of(role));

            final Publisher<String> applicationAsStream = Source
                .single(application)
                .map(obj -> String.join(
                    "\n",
                    Lists.newArrayList(obj.build, obj.name)))
                .runWith(Sink.asPublisher(false), materializer);

            final Function<User, Publisher<String>> userAsStream = u -> Source
                .single(u)
                .map(obj -> u.toString())
                .runWith(Sink.asPublisher(false), materializer);

            return AboutResource.FakeImpl.apply(application, applicationAsStream, userAsStream);
        }

    }

    @Test
    public void testGetAboutAsObject() {
        assertThat(
            this
                .restTemplate
                .getForObject(getAboutUrl(), Application.class))

            .isNotNull()
            .hasNoNullFieldsOrProperties()
            .matches(a -> a.getBuild().equals(build), "Should equal the configured build number")
            .matches(a -> a.getName().equals(name), "Should respond wih name from configuration");
    }

    @Test
    public void testGetAboutAsStream() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Lists.newArrayList(MediaType.TEXT_EVENT_STREAM));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        assertThat(
            this
                .restTemplate
                .exchange(getAboutUrl(), HttpMethod.GET, entity, String.class)
                .getBody())

            .contains(build)
            .contains(name);
    }

    @Test
    public void getAboutUserAsObject$testAuthenticatedUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        headers.set("x-user-id", username);
        headers.set("x-roles-allowed", role);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        assertThat(
            this
                .restTemplate
                .exchange(getAboutUserUrl(), HttpMethod.GET, entity, AuthenticatedUser.class)
                .getBody())

            .isNotNull()
            .hasNoNullFieldsOrProperties()
            .matches(u -> u.getUsername().equals(username), "username should be as setup by stub")
            .matches(u -> u.getRoles().contains(role), "role set up by stub should be contained");
    }

    @Test
    public void getAboutUserAsObject$testAnonymousUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        headers.set("x-roles-allowed", role);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        assertThat(
            this
                .restTemplate
                .exchange(getAboutUserUrl(), HttpMethod.GET, entity, AnonymousUser.class)
                .getBody())

            .isNotNull()
            .hasNoNullFieldsOrProperties()
            .matches(u -> u.getRoles().contains(role), "role set up by stub should be contained");
    }

    @Test
    public void getAboutUserAsStream$testAuthenticatedUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Lists.newArrayList(MediaType.TEXT_EVENT_STREAM));
        headers.set("x-user-id", username);
        headers.set("x-roles-allowed", role);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        assertThat(
            this
                .restTemplate
                .exchange(getAboutUserUrl(), HttpMethod.GET, entity, String.class)
                .getBody())

            .isNotNull()
            .contains(username)
            .contains(role);
    }

    @Test
    public void getAboutUserAsStream$testAnonymousUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Lists.newArrayList(MediaType.TEXT_EVENT_STREAM));
        headers.set("x-roles-allowed", role);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        assertThat(
            this
                .restTemplate
                .exchange(getAboutUserUrl(), HttpMethod.GET, entity, String.class)
                .getBody())

            .isNotNull()
            .doesNotContain(username)
            .contains(role);
    }

    private String getAboutUrl() {
        return "http://localhost:" + port + "/api/v1/about";
    }

    private String getAboutUserUrl() {
        return getAboutUrl() + "/user";
    }

}
