package ada.vcs.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.values.Authorization;
import ada.vcs.domain.dvc.values.User;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.events.GrantedAccessToRepository;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class GrantAccessToRepository implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final Authorization authorization;

    private final ActorRef<GrantedAccessToRepository> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
