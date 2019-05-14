package ada.vcs.server.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.NamespaceEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositoryRemoved implements NamespaceEvent {

    private final ResourceName namespace;

    private final ResourceName repository;

    @JsonCreator
    public static RepositoryRemoved apply(
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("repository") ResourceName repository) {

        return new RepositoryRemoved(namespace, repository);
    }

}
