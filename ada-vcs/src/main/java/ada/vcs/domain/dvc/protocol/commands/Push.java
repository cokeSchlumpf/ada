package ada.vcs.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.values.User;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.legacy.repository.api.RepositorySinkMemento;
import ada.vcs.domain.legacy.repository.api.version.VersionDetailsMemento;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class Push implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final VersionDetailsMemento details;

    private final ActorRef<RepositorySinkMemento> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
