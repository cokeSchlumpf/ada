package ada.vcs.server.domain.dvc.values;

import ada.commons.util.ResourceName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;
import java.util.Optional;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositorySummary {

    private final ResourceName namespace;

    private final ResourceName repository;

    private final Date lastUpdate;

    private final String latestId;

    public static RepositorySummary apply(ResourceName namespace, ResourceName repository, Date lastUpdate) {
        return apply(namespace, repository, lastUpdate, null);
    }

    public Optional<String> getLatestId() {
        return Optional.ofNullable(latestId);
    }

}
