package ada.vcs.server.domain.dvc.protocol.queries;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.server.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoriesInNamespaceRequest implements NamespaceMessage {

    private final User executor;

    private final ResourceName namespace;

    private final ActorRef<RepositoriesInNamespaceResponse> replyTo;

}
