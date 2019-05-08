package ada.vcs.shared.repository.api;

import ada.commons.util.ResourceName;
import ada.vcs.shared.repository.api.version.VersionDetails;

public interface RepositoryStorageAdapter {

    RepositorySinkMemento push(
        ResourceName namespace, ResourceName repository, VersionDetails version);

    RepositorySourceMemento pull(
        ResourceName namespace, ResourceName repository, RefSpec.VersionRef version);

}
