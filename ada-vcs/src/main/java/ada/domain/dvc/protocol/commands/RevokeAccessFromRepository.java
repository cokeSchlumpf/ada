package ada.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.protocol.events.RevokedAccessFromRepository;
import ada.domain.dvc.values.Authorization;
import ada.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RevokeAccessFromRepository implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final Authorization authorization;

    private final ActorRef<RevokedAccessFromRepository> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
