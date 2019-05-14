package ada.vcs.domain.dvc.services;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.values.User;
import ada.vcs.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesResponse;
import akka.actor.typed.ActorRef;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Map;

@Value
@AllArgsConstructor(staticName = "apply")
public final class StartRepositoriesQuery {

    private final User executor;

    private final ActorRef<RepositoriesResponse> replyTo;

    private final ImmutableMap<ResourceName, ActorRef<NamespaceMessage>> namespaces;

    public static StartRepositoriesQuery apply(
        User executor,
        ActorRef<RepositoriesResponse> replyTo,
        Map<ResourceName, ActorRef<NamespaceMessage>> namespaces) {

        return apply(executor, replyTo, ImmutableMap.copyOf(namespaces));
    }

}
