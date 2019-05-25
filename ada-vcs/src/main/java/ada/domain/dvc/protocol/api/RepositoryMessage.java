package ada.domain.dvc.protocol.api;

import ada.commons.util.ResourceName;
import ada.domain.dvc.values.User;

public interface RepositoryMessage extends NamespaceMessage {

    String getId();

    ResourceName getRepository();

    User getExecutor();

}
