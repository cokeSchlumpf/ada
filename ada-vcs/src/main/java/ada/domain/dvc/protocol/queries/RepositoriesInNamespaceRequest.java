package ada.domain.dvc.protocol.queries;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.NamespaceMessage;
import ada.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoriesInNamespaceRequest implements NamespaceMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ActorRef<RepositoriesInNamespaceResponse> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
