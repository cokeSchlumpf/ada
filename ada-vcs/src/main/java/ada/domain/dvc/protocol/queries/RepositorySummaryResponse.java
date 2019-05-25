package ada.domain.dvc.protocol.queries;

import ada.commons.util.ResourceName;
import ada.domain.dvc.values.RepositorySummary;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositorySummaryResponse {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final RepositorySummary summary;

    public static RepositorySummaryResponse apply(String id, ResourceName namespace, ResourceName repository) {
        return apply(id, namespace, repository, null);
    }

    public Optional<RepositorySummary> getSummary() {
        return Optional.ofNullable(summary);
    }

}
