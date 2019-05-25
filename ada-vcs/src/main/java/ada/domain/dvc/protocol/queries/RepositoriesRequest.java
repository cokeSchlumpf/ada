package ada.domain.dvc.protocol.queries;

import ada.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoriesRequest implements DataVersionControlMessage {

    private final User executor;

    private final ActorRef<RepositoriesResponse> replyTo;

}
