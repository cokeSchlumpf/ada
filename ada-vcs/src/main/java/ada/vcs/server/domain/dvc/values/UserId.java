package ada.vcs.server.domain.dvc.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserId {

    private final String id;

    private final String name;

    @JsonCreator
    public static UserId apply(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name) {

        return new UserId(id, name);
    }

}
