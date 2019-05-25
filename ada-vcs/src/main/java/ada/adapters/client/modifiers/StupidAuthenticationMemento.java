package ada.adapters.client.modifiers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class StupidAuthenticationMemento implements AuthenticationMethodMemento {

    private final String username;

    private final List<String> roles;

    public static StupidAuthenticationMemento apply(
        @JsonProperty("username") String username,
        @JsonProperty("roles") List<String> roles) {

        return new StupidAuthenticationMemento(username, roles);
    }

}
