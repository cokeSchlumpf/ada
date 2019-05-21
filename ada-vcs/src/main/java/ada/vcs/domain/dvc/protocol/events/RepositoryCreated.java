package ada.vcs.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.RepositoryEvent;
import ada.vcs.domain.dvc.protocol.values.UserId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepositoryCreated implements RepositoryEvent {

    private static final String CREATED = "created";
    private static final String ID = "id";
    private static final String NAMESPACE = "namespace";
    private static final String REPOSITORY = "repository";
    private static final String USER = "user";

    @JsonProperty(ID)
    private final String id;

    @JsonProperty(NAMESPACE)
    private final ResourceName namespace;

    @JsonProperty(REPOSITORY)
    private final ResourceName repository;

    @JsonProperty(USER)
    private final UserId userId;

    @JsonProperty(CREATED)
    private final Date created;

    @JsonCreator
    public static RepositoryCreated apply(
        @JsonProperty(ID) String id,
        @JsonProperty(NAMESPACE) ResourceName namespace,
        @JsonProperty(REPOSITORY) ResourceName repository,
        @JsonProperty(USER) UserId userId,
        @JsonProperty(CREATED) Date created) {

        return new RepositoryCreated(id, namespace, repository, userId, created);
    }

}
