package ada.vcs.server.domain.dvc.services;

import ada.commons.util.ResourceName;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositoriesQueryError {

    private final ResourceName namespace;

    private final ResourceName repository;

}
