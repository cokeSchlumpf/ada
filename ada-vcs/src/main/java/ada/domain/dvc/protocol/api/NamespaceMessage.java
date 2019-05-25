package ada.domain.dvc.protocol.api;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import akka.actor.typed.ActorRef;

public interface NamespaceMessage extends DataVersionControlMessage {

    String getId();

    ResourceName getNamespace();

    ActorRef<ErrorMessage> getErrorTo();

}
