package ada.web.impl.resources.about;

import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.AnonymousUser;
import ada.web.api.resources.about.model.AuthenticatedUser;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AboutResourceUTest {

    private static final String name = "ada-test";

    private static final String build = "0.0.42";

    private static final AuthenticatedUser user = AuthenticatedUser.apply("Biene Maja", ImmutableSet.of("foo", "bar"));

    private ActorSystem system;

    private Materializer mat;

    @After
    public void after() {
        if (mat != null) {
            ((ActorMaterializer) mat).shutdown();
        }
        if (system != null) {
            system.terminate();
        }
    }

    @Before
    public void before() {
        system = ActorSystem.create("ada-test");
        mat = ActorMaterializer.create(system);
    }

    @Test
    public void getApplication$test() {
        AboutConfiguration configuration = AboutConfiguration.FakeImpl.apply(name, build);
        AboutResource resource = AboutResourceFactory.create(configuration, mat);

        assertThat(resource.getApplicationAsObject())
            .isNotNull()
            .matches(a -> a.getName().equals(name))
            .matches(a -> a.getBuild().equals(build));
    }

    @Test
    public void getApplicationAsStream$test() throws ExecutionException, InterruptedException {
        AboutConfiguration configuration = AboutConfiguration.FakeImpl.apply(name, build);
        AboutResource resource = AboutResourceFactory.create(configuration, mat);

        String lines = Source
            .fromPublisher(resource.getApplicationAsStream())
            .map(s -> s)
            .runWith(
                Sink.fold(
                    ImmutableList.<String>builder(),
                    ImmutableList.Builder::add),
                mat)
            .thenApply(ImmutableList.Builder::build)
            .toCompletableFuture()
            .get()
            .stream()
            .collect(Collectors.joining("\n"));

        assertThat(lines)
            .isNotNull()
            .contains(name)
            .contains(build);
    }

    @Test
    public void getUser$test() {
        AboutConfiguration configuration = AboutConfiguration.FakeImpl.apply(name, build);
        AboutResource resource = AboutResourceFactory.create(configuration, mat);

        assertThat(resource.getUser(user))
            .isEqualTo(user);
    }

    @Test
    public void getUserAsStream$testAuthenticated() throws ExecutionException, InterruptedException {
        AboutConfiguration configuration = AboutConfiguration.FakeImpl.apply(name, build);
        AboutResource resource = AboutResourceFactory.create(configuration, mat);

        String lines = Source
            .fromPublisher(resource.getUserAsStream(user))
            .map(s -> s)
            .runWith(
                Sink.fold(
                    ImmutableList.<String>builder(),
                    ImmutableList.Builder::add),
                mat)
            .thenApply(ImmutableList.Builder::build)
            .toCompletableFuture()
            .get()
            .stream()
            .collect(Collectors.joining("\n"));

        assertThat(lines)
            .isNotNull()
            .contains(user.getRoles().iterator().next())
            .contains(user.getUsername());
    }

    @Test
    public void getUserAsStream$testUnauthenticatedWithRoles() throws ExecutionException, InterruptedException {
        AboutConfiguration configuration = AboutConfiguration.FakeImpl.apply(name, build);
        AboutResource resource = AboutResourceFactory.create(configuration, mat);
        AnonymousUser user = AnonymousUser.apply(ImmutableSet.of(this.user.getRoles().iterator().next()));

        String lines = Source
            .fromPublisher(resource.getUserAsStream(user))
            .map(s -> s)
            .runWith(
                Sink.fold(
                    ImmutableList.<String>builder(),
                    ImmutableList.Builder::add),
                mat)
            .thenApply(ImmutableList.Builder::build)
            .toCompletableFuture()
            .get()
            .stream()
            .collect(Collectors.joining("\n"));

        assertThat(lines)
            .isNotNull()
            .contains(user.getRoles().iterator().next());
    }

    @Test
    public void getUserAsStream$testUnauthenticatedWithoutRoles() throws ExecutionException, InterruptedException {
        AboutConfiguration configuration = AboutConfiguration.FakeImpl.apply(name, build);
        AboutResource resource = AboutResourceFactory.create(configuration, mat);
        AnonymousUser user = AnonymousUser.apply(ImmutableSet.of());

        String lines = Source
            .fromPublisher(resource.getUserAsStream(user))
            .map(s -> s)
            .runWith(
                Sink.fold(
                    ImmutableList.<String>builder(),
                    ImmutableList.Builder::add),
                mat)
            .thenApply(ImmutableList.Builder::build)
            .toCompletableFuture()
            .get()
            .stream()
            .collect(Collectors.joining("\n"));

        assertThat(lines)
            .isNotNull()
            .doesNotContain(this.user.getRoles().iterator().next());
    }

}
