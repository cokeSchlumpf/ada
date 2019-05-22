package ada.vcs.adapters.client;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.client.modifiers.RequestModifiers;
import ada.vcs.adapters.client.repositories.RepositoriesClient;
import ada.vcs.client.util.AbstractAdaTest;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesResponse;
import ada.vcs.domain.dvc.values.GrantedAuthorization;
import ada.vcs.domain.dvc.values.UserAuthorization;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


public class RepositoriesClientUTest extends AbstractAdaTest {

    @Test
    public void test() throws MalformedURLException, ExecutionException, InterruptedException {
        RepositoriesClient client = getClient();

        RepositoriesResponse result = client
            .createRepository(ResourceName.apply("public"), ResourceName.apply("happy-hippo"))
            .thenCompose(i -> client.listRepositories())
            .toCompletableFuture()
            .get();

        // Wait a second to allow the cluster syncing
        Thread.sleep(1000);

        assertThat(result.getRepositories()).hasSize(1);
        assertThat(result.getRepositories().get(0).getNamespace()).isEqualTo(ResourceName.apply("public"));
        assertThat(result.getRepositories().get(0).getRepository()).isEqualTo(ResourceName.apply("happy-hippo"));
    }

    @Test
    public void listRepositories() throws MalformedURLException, ExecutionException, InterruptedException {
        RepositoriesClient client = getClient();

        RepositoriesResponse repositoriesResponse = client
            .listRepositories()
            .thenApply(repos -> {
                System.out.println(repos);
                return repos;
            })
            .toCompletableFuture()
            .get();

        // Wait a second to allow the cluster syncing
        Thread.sleep(1000);

        System.out.println(repositoriesResponse);
    }

    @Test
    public void grant() throws MalformedURLException, ExecutionException, InterruptedException {
        RepositoriesClient client = getClient();
        ResourceName ns = ResourceName.apply("public");
        ResourceName repo = ResourceName.apply("foo");

        GrantedAuthorization hippo = client
            .createRepository(ns, repo)
            .thenApply(rc -> Operators.suppressExceptions(() -> rc
                .grant(UserAuthorization.apply("hippo"))
                .toCompletableFuture()
                .get()))
            .toCompletableFuture()
            .get();

        System.out.println(hippo);
    }

    private RepositoriesClient getClient() throws MalformedURLException {
        ObjectMapper om = ObjectMapperFactory.apply().create(true);
        Materializer materializer = getContext().materializer();

        return RepositoriesClient.apply(
            new URL("http://" + getServer()), getContext().system(),
            materializer,
            om,
            getContext().factories().versionFactory(),
            RequestModifiers.apply(),
            ExceptionHandler.apply(om, materializer));
    }

}
