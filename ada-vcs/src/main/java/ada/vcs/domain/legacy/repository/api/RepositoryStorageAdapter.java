package ada.vcs.domain.legacy.repository.api;

import ada.commons.util.ResourceName;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import akka.Done;

import java.util.concurrent.CompletionStage;

public interface RepositoryStorageAdapter {

    CompletionStage<Done> clean(
        ResourceName namespace, ResourceName repository);

    CompletionStage<Done> clean(
        ResourceName namespace, ResourceName repository, RefSpec.VersionRef version);

    RepositorySinkMemento push(
        ResourceName namespace, ResourceName repository, VersionDetails version);

    RepositorySourceMemento pull(
        ResourceName namespace, ResourceName repository, RefSpec.VersionRef version);

}
