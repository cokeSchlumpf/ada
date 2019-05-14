package ada.vcs.server.domain.dvc.protocol.api;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.errors.UserNotAuthorizedError;
import ada.vcs.server.domain.dvc.values.User;
import akka.actor.typed.ActorRef;

public interface RepositoryMessage extends NamespaceMessage {

    String getId();

    ResourceName getRepository();

    User getExecutor();

}
