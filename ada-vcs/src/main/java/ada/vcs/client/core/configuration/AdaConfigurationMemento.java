package ada.vcs.client.core.configuration;

import ada.vcs.client.core.repository.api.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdaConfigurationMemento {

    User user;

    @JsonCreator
    public static AdaConfigurationMemento apply(
        @JsonProperty("user") User user) {

        return new AdaConfigurationMemento(user);
    }

}
