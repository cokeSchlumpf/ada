package ada.vcs.domain.legacy.repository.api;

import ada.commons.util.ResourceName;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import akka.Done;

import java.util.concurrent.CompletionStage;

public interface RepositoryStorageAdapter {

    CompletionStage<Done> clean();

    CompletionStage<Done> clean(RefSpec.VersionRef version);

    RepositorySinkMemento push(
        ResourceName namespace, ResourceName repository, VersionDetails version);

    RepositorySourceMemento pull(
        ResourceName namespace, ResourceName repository, RefSpec.VersionRef version);

}
