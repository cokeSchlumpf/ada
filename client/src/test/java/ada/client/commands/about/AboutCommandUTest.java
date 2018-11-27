package ada.client.commands.about;

import ada.client.output.StringOutput;
import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.AuthenticatedUser;
import ada.web.api.resources.about.model.User;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.Sink;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

public class AboutCommandUTest {

    private final static String name = "pyt-test";

    private final static String build = "0.0.42";

    private final static String username = "egon olsen";

    private final static String role = "admin";

    private AboutCommand command;

    private ActorSystem system;

    private Materializer materializer;

    private StringOutput out = StringOutput.apply();

    @Before
    public void before() {
        this.system = ActorSystem.apply("test");
        this.materializer = ActorMaterializer.create(system);

        User user = AuthenticatedUser.apply(username, ImmutableSet.of(role));
        AboutResource aboutResource = getAboutResource();

        this.out = StringOutput.apply();
        this.command = AboutCommandImpl.apply(aboutResource, materializer, out, user);
    }

    @After
    public void after() {
        if (this.command != null) {
            this.command = null;
        }

        if (this.materializer != null) {
            ((ActorMaterializer) this.materializer).shutdown();
            this.materializer = null;
        }

        if (this.system != null) {
            this.system.terminate();
            this.system = null;
        }
    }

    @Test
    public void about() {
        command.about();

        assertThat(out.toString()).contains(build)
            .contains(name);
    }

    @Test
    public void aboutUser() {
        command.aboutUser();

        assertThat(out.toString()).contains(username)
            .contains(role);
    }

    private AboutResource getAboutResource() {
        final Application application = Application.apply(name, build);

        final AuthenticatedUser user = AuthenticatedUser.apply(username, ImmutableSet.of(role));

        final Publisher<String> applicationAsStream = Source
            .single(application)
            .map(obj -> String.join(
                "\n",
                Lists.newArrayList(
                    obj.build,
                    obj.name)))
            .runWith(
                Sink.asPublisher(false),
                materializer);

        final Function<User, Publisher<String>> userAsStream = u -> Source
            .single(u)
            .map(obj -> u.toString())
            .runWith(
                Sink.asPublisher(false),
                materializer);

        return AboutResource.FakeImpl.apply(
            application,
            applicationAsStream,
            userAsStream);
    }

}
