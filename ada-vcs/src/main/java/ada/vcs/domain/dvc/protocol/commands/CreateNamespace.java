package ada.vcs.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.domain.dvc.protocol.events.NamespaceCreated;
import ada.vcs.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class CreateNamespace implements NamespaceMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ActorRef<NamespaceCreated> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
