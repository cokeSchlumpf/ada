package ada.vcs.domain.dvc.protocol.queries;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.values.RepositoryAuthorizations;
import ada.vcs.domain.dvc.protocol.values.RepositorySummary;
import ada.vcs.domain.dvc.protocol.values.VersionStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositoryDetailsResponse {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final RepositorySummary summary;

    private final RepositoryAuthorizations authorizations;

    private final ImmutableList<VersionStatus> versions;

    @JsonCreator
    public static RepositoryDetailsResponse apply(
        @JsonProperty("id") String id,
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("repository") ResourceName repository,
        @JsonProperty("summary") RepositorySummary summary,
        @JsonProperty("authorizations") RepositoryAuthorizations authorizations,
        @JsonProperty("versions") List<VersionStatus> versions) {

        return new RepositoryDetailsResponse(
            id, namespace, repository, summary,
            authorizations, ImmutableList.copyOf(versions));
    }

}
