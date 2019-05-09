package ada.vcs.server.domain.dvc.protocol.queries;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.errors.RefSpecNotFoundError;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.values.User;
import ada.vcs.shared.repository.api.RefSpec;
import ada.vcs.shared.repository.api.RepositorySourceMemento;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class Pull implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final RefSpec refSpec;

    private final ActorRef<RepositorySourceMemento> replyTo;

    private final ActorRef<RefSpecNotFoundError> handleError;

}
