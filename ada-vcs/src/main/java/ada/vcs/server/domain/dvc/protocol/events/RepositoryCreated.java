package ada.vcs.server.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoryCreated {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final ActorRef<RepositoryMessage> repositoryActor;

}
