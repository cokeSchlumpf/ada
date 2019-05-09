package ada.vcs.server.domain.dvc.protocol.api;

import ada.commons.util.ResourceName;

public interface NamespaceMessage extends DataVersionControlMessage {

    ResourceName getNamespace();

}
