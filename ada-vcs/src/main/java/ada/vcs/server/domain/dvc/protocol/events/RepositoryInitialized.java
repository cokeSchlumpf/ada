package ada.vcs.server.domain.dvc.protocol.events;

import ada.vcs.server.domain.dvc.protocol.api.RepositoryEvent;
import ada.vcs.server.domain.dvc.values.UserId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepositoryInitialized implements RepositoryEvent {

    private final Date date;

    private final UserId user;

    @JsonCreator
    public static RepositoryInitialized apply(
        @JsonProperty("date") Date date,
        @JsonProperty("user") UserId user) {

        return new RepositoryInitialized(date, user);
    }

}
