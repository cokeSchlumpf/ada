package ada.vcs.domain.dvc.protocol.values;

import ada.vcs.domain.dvc.protocol.api.ValueObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserId implements ValueObject {

    private final String id;

    private final String name;

    @JsonCreator
    public static UserId apply(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name) {

        return new UserId(id, name);
    }

}
