package ada.vcs.domain.dvc.protocol.api;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.values.User;

public interface RepositoryMessage extends NamespaceMessage {

    String getId();

    ResourceName getRepository();

    User getExecutor();

}
