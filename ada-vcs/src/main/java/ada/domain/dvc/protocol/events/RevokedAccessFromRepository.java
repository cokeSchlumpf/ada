package ada.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.RepositoryEvent;
import ada.domain.dvc.values.GrantedAuthorization;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RevokedAccessFromRepository implements RepositoryEvent {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final GrantedAuthorization authorization;

    @JsonCreator
    public static RevokedAccessFromRepository apply(
        @JsonProperty("id") String id,
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("repository") ResourceName repository,
        @JsonProperty("authorization") GrantedAuthorization authorization) {

        return new RevokedAccessFromRepository(id, namespace, repository, authorization);
    }

}
