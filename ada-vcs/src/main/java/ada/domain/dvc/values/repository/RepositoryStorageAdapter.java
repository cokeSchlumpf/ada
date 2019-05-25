package ada.domain.dvc.values.repository;

import ada.commons.util.ResourceName;
import ada.domain.dvc.values.repository.version.VersionDetails;
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
