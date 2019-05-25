package ada.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.RepositoryEvent;
import ada.domain.dvc.values.repository.VersionStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionUpsertedInRepository implements RepositoryEvent {

    private final ResourceName namespace;

    private final ResourceName repository;

    private final VersionStatus status;

    @JsonCreator
    public static VersionUpsertedInRepository apply(
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("repository") ResourceName repository,
        @JsonProperty("status") VersionStatus status) {

        return new VersionUpsertedInRepository(namespace, repository, status);
    }

}
