package ada.cli.commands.repository.client;

import ada.cli.restclient.RestClient;
import com.ibm.ada.api.exceptions.NotAuthorizedException;
import com.ibm.ada.api.repository.RepositoryAdministration;
import com.ibm.ada.model.RepositoryDetails;
import com.ibm.ada.model.RepositoryName;
import com.ibm.ada.model.auth.AuthorizationRequest;
import com.ibm.ada.model.auth.User;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class RepositoryAdministrationClient implements RepositoryAdministration {

    private final RestClient client;

    private final RepositoryName name;

    @Override
    public CompletionStage<RepositoryDetails> changeOwner(User executor, AuthorizationRequest auth) {
        String uri = String.format("/api/v1/repositories/%s/owner", name);
        return client.post(uri, RepositoryDetails.class, auth).future();
    }

    @Override
    public CompletionStage<Void> delete(User executor) {
        String uri = String.format("/api/v1/repositories/%s", name);
        return client.delete(uri, Void.class).future();
    }

    @Override
    public CompletionStage<RepositoryDetails> grant(User executor, AuthorizationRequest auth) {
        String uri = String.format("/api/v1/repositories/%s/access", name);
        return client.put(uri, RepositoryDetails.class, auth).future();
    }

    @Override
    public CompletionStage<RepositoryDetails> revoke(User executor, AuthorizationRequest auth) {
        String uri = String.format("/api/v1/repositories/%s/access", name);
        return client.delete(uri, RepositoryDetails.class).future();
    }

}
