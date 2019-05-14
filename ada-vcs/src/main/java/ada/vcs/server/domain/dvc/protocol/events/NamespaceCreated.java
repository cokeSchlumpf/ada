package ada.vcs.server.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.DataVersionControlEvent;
import ada.vcs.server.domain.dvc.values.UserId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NamespaceCreated implements DataVersionControlEvent {

    private final String id;

    private final ResourceName namespace;

    private final UserId user;

    private final Date created;

    @JsonCreator
    public static NamespaceCreated apply(
        @JsonProperty("id") String id,
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("user") UserId user,
        @JsonProperty("created") Date created) {

        return new NamespaceCreated(id, namespace, user, created);
    }

}
