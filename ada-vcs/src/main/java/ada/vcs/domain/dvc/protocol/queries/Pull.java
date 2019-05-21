package ada.vcs.domain.dvc.protocol.queries;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.values.User;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.legacy.repository.api.RefSpec;
import ada.vcs.domain.legacy.repository.api.RepositorySourceMemento;
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

    private final ActorRef<ErrorMessage> errorTo;

}
