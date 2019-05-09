package ada.vcs.server.domain.dvc.protocol.api;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.values.User;

public interface RepositoryMessage extends NamespaceMessage {

    ResourceName getRepository();

    User getExecutor();

}
