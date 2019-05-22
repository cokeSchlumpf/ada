package ada.vcs.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
@AllArgsConstructor(staticName = "apply")
public final class CreateRepository implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final ActorRef<RepositoryCreated> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
