package ada.vcs.server.domain.dvc.protocol.commands;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.server.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.server.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class CreateRepository implements NamespaceMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final ActorRef<RepositoryCreated> replyTo;

}
