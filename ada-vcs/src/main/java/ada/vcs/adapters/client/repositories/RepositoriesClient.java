package ada.vcs.adapters.client.repositories;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.client.modifiers.RequestModifier;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesResponse;
import ada.vcs.domain.legacy.repository.api.version.VersionFactory;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.net.URL;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class RepositoriesClient {

    private final URL endpoint;

    private final ActorSystem system;

    private final Materializer materializer;

    private final ObjectMapper om;

    private final VersionFactory versionFactory;

    private final RequestModifier modifier;

    public CompletionStage<RepositoryClient> createRepository(ResourceName namespace, ResourceName repository) {
        URL repoUrl = Operators
            .suppressExceptions(() -> new URL(endpoint, namespace.getValue() + "/" + repository.getValue()));

        return modifier
            .modifyClient(Http.get(system))
            .singleRequest(modifier.modifyRequest(HttpRequest.PUT(repoUrl.toString())))
            .thenApply(httpResponse -> RepositoryClient.apply(repoUrl, system, materializer, om, versionFactory, modifier));
    }

    public CompletionStage<RepositoriesResponse> listRepositories() {
        return modifier
            .modifyClient(Http.get(system))
            .singleRequest(modifier.modifyRequest(HttpRequest.GET(endpoint.toString())))
            .thenCompose(response -> Jackson
                .unmarshaller(om, RepositoriesResponse.class)
                .unmarshal(response.entity().withoutSizeLimit(), materializer));
    }

}
