package ada.vcs.domain.dvc.protocol.values;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.ValueObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositorySummary implements ValueObject {

    private final ResourceName namespace;

    private final ResourceName repository;

    private final Date lastUpdate;

    private final String latestId;

    @JsonCreator
    public static RepositorySummary apply(
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("repository") ResourceName repository,
        @JsonProperty("lastUpdate") Date lastUpdate,
        @JsonProperty("latestId") String latestId) {

        return new RepositorySummary(namespace, repository, lastUpdate, latestId);
    }

    public static RepositorySummary apply(ResourceName namespace, ResourceName repository, Date lastUpdate) {
        return apply(namespace, repository, lastUpdate, null);
    }

    public Optional<String> getLatestId() {
        return Optional.ofNullable(latestId);
    }

}
