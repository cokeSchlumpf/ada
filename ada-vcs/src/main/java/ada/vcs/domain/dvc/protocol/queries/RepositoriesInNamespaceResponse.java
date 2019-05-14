package ada.vcs.domain.dvc.protocol.queries;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import akka.actor.typed.ActorRef;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Map;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositoriesInNamespaceResponse {

    private final ResourceName namespace;

    private final ImmutableMap<ResourceName, ActorRef<RepositoryMessage>> repositories;

    public static RepositoriesInNamespaceResponse apply(
        ResourceName namespace,
        Map<ResourceName, ActorRef<RepositoryMessage>> repositories) {

        return apply(namespace, ImmutableMap.copyOf(repositories));
    }

}
