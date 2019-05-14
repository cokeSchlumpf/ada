package ada.vcs.server.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryEvent;
import ada.vcs.server.domain.dvc.values.GrantedAuthorization;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.control.TreeSortMode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RevokedAccessToRepository implements RepositoryEvent {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final GrantedAuthorization authorization;

    @JsonCreator
    public static RevokedAccessToRepository apply(
        @JsonProperty("id") String id,
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("repository") ResourceName repository,
        @JsonProperty("authorization") GrantedAuthorization authorization) {

        return new RevokedAccessToRepository(id, namespace, repository, authorization);
    }

}
