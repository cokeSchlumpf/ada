package ada.vcs.client.core.repository.api.version;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagMemento {

    private final ResourceName alias;

    private final User user;

    private final Date date;

    @JsonCreator
    public static TagMemento apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("user") User user,
        @JsonProperty("date") Date date) {

        return new TagMemento(alias, user, date);
    }

}
