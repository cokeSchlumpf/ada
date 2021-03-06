package ada.adapters.client.repositories;

import ada.adapters.client.ExceptionHandler;
import ada.commons.util.Operators;
import ada.adapters.client.modifiers.RequestModifier;
import ada.adapters.client.modifiers.RequestModifiers;
import ada.domain.dvc.values.repository.version.VersionFactory;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.net.URL;

@AllArgsConstructor(staticName = "apply")
public final class RepositoriesClientFactory {

    private final ActorSystem system;

    private final Materializer materializer;

    private final ObjectMapper om;

    private final VersionFactory versionFactory;

    public RepositoriesClient createRepositories(URL endpoint, RequestModifier ...modifiers) {
        return RepositoriesClient.apply(
            endpoint, system, materializer, om, versionFactory,
            RequestModifiers.apply(modifiers), ExceptionHandler.apply(om, materializer));
    }

    public RepositoriesClient createRepositories(String endpoint, RequestModifier ...modifiers) {
        return createRepositories(Operators.suppressExceptions(() -> new URL(endpoint)), modifiers);
    }

    public RepositoryClient createRepository(URL endpoint, RequestModifier ...modifiers) {
        return RepositoryClient.apply(
            endpoint, system, materializer, om, versionFactory,
            RequestModifiers.apply(modifiers), ExceptionHandler.apply(om, materializer));
    }

    public RepositoryClient createRepository(String endpoint, RequestModifier ...modifiers) {
        return createRepository(Operators.suppressExceptions(() -> new URL(endpoint)), modifiers);
    }

}
