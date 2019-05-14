package ada.vcs.server.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.NamespaceMessage;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class RemoveNamespace implements NamespaceMessage {

    private final String id;

    private final ResourceName namespace;

    private final ActorRef<ErrorMessage> errorTo;

}
