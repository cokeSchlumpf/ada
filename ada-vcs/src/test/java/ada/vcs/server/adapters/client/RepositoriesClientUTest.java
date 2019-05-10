package ada.vcs.server.adapters.client;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.ResourceName;
import ada.vcs.client.util.AbstractAdaTest;
import ada.vcs.server.adapters.client.repositories.RepositoriesClient;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesResponse;
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

        assertThat(result.getRepositories()).hasSize(1);
        assertThat(result.getRepositories().get(0).getNamespace()).isEqualTo(ResourceName.apply("public"));
        assertThat(result.getRepositories().get(0).getRepository()).isEqualTo(ResourceName.apply("happy-hippo"));
    }

    @Test
    public void listRepositories() throws MalformedURLException, ExecutionException, InterruptedException {
        RepositoriesClient client = getClient();

        RepositoriesResponse repositoriesResponse = client
            .listRepositories()
            .toCompletableFuture()
            .get();

        System.out.println(repositoriesResponse);
    }

    private RepositoriesClient getClient() throws MalformedURLException {
        return RepositoriesClient.apply(
            new URL("http://" + getServer()), getContext().system(),
            getContext().materializer(), ObjectMapperFactory.apply().create(true));
    }

}
