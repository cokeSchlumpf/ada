package ada.vcs.server.domain.repository.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserAuthorization implements Authorization {

    private final String name;

    @JsonCreator
    public static UserAuthorization apply(@JsonProperty("name") String name) {
        return new UserAuthorization(name);
    }

    @Override
    public boolean hasAuthorization(User user) {
        if (user instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) user).getName().equals(name);
        } else {
            return false;
        }
    }
    
}
