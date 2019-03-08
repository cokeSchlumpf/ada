package ada.cli.commands.repository.client;

import ada.cli.restclient.RestClient;
import com.ibm.ada.api.exceptions.RepositoryNotFoundException;
import com.ibm.ada.api.repository.Repositories;
import com.ibm.ada.api.repository.Repository;
import com.ibm.ada.model.RepositoryDetails;
import com.ibm.ada.model.RepositoryName;
import com.ibm.ada.model.auth.User;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.stream.Stream;

@AllArgsConstructor(staticName = "apply")
public final class RepositoriesClient implements Repositories {

    private final RestClient client;

    @Override
    public Repository createRepository(User executor, RepositoryName name) {
        String uri = String.format("/api/v1/repositories/%s", name.getValue());

        RepositoryDetails details = client
            .put(uri, RepositoryDetails.class)
            .await();

        return RepositoryClient.apply(client, details);
    }

    @Override
    public Repository getRepository(User executor, RepositoryName name) throws RepositoryNotFoundException {
        String uri = String.format("/api/v1/repositories/%s", name.getValue());

        RepositoryDetails details = client
            .get(uri, RepositoryDetails.class)
            .await(RepositoryNotFoundException.class);

        return RepositoryClient.apply(client, details);
    }

    @Override
    public Stream<Repository> getRepositories(User executor) {
        String uri = "/api/v1/repositories";

        return Flux
            .from(client
                .get(uri, RepositoryDetails.class)
                .publisher())
            .map(details -> (Repository) RepositoryClient.apply(client, details))
            .toStream();
    }

}
