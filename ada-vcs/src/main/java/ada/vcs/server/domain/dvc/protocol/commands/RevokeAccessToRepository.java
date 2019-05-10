package ada.vcs.server.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.protocol.events.RevokedAccessToRepository;
import ada.vcs.server.domain.dvc.values.Authorization;
import ada.vcs.server.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RevokeAccessToRepository implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final Authorization authorization;

    private final ActorRef<RevokedAccessToRepository> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}