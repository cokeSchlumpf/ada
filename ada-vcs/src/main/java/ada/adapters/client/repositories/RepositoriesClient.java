package ada.adapters.client.repositories;

import ada.adapters.client.ExceptionHandler;
import ada.adapters.client.modifiers.RequestModifier;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.queries.RepositoriesResponse;
import ada.domain.legacy.repository.api.version.VersionFactory;
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

    private final ExceptionHandler exceptionHandler;

    public CompletionStage<RepositoryClient> createRepository(ResourceName namespace, ResourceName repository) {
        URL repoUrl = Operators
            .suppressExceptions(() -> new URL(endpoint, namespace.getValue() + "/" + repository.getValue()));

        return modifier
            .modifyClient(Http.get(system))
            .singleRequest(modifier.modifyRequest(HttpRequest.PUT(repoUrl.toString())))
            .thenCompose(exceptionHandler::handle)
            .thenApply(httpResponse -> RepositoryClient.apply(
                repoUrl, system, materializer, om, versionFactory, modifier, exceptionHandler));
    }

    public RepositoryClient getRepository(ResourceName namespace, ResourceName repository) {
        URL repoUrl = Operators
            .suppressExceptions(() -> new URL(endpoint, namespace.getValue() + "/" + repository.getValue()));

        return RepositoryClient.apply(repoUrl, system, materializer, om, versionFactory, modifier, exceptionHandler);
    }

    public CompletionStage<RepositoriesResponse> listRepositories() {
        return modifier
            .modifyClient(Http.get(system))
            .singleRequest(modifier.modifyRequest(HttpRequest.GET(endpoint.toString())))
            .thenCompose(exceptionHandler::handle)
            .thenCompose(response -> Jackson
                .unmarshaller(om, RepositoriesResponse.class)
                .unmarshal(response.entity().withoutSizeLimit(), materializer));
    }

}
