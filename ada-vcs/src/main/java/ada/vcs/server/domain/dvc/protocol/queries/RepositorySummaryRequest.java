package ada.vcs.server.domain.dvc.protocol.queries;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositorySummaryRequest implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final ActorRef<RepositorySummaryResponse> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
