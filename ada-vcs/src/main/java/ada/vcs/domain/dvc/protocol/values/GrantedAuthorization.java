package ada.vcs.domain.dvc.protocol.values;

import ada.vcs.domain.dvc.protocol.api.ValueObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrantedAuthorization implements ValueObject {

    User by;

    Date at;

    Authorization authorization;

    @JsonCreator
    public static GrantedAuthorization apply(
        @JsonProperty("by") User by,
        @JsonProperty("at") Date at,
        @JsonProperty("authorization") Authorization authorization) {

        return new GrantedAuthorization(by, at, authorization);
    }

}
