package ada.vcs.domain.dvc.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleAuthorization implements Authorization {

    private final String name;

    @JsonCreator
    public static RoleAuthorization apply(@JsonProperty("name") String name) {
        return new RoleAuthorization(name);
    }

    @Override
    public boolean hasAuthorization(User user) {
        return user.getRoles().contains(name);
    }

    public String toString() {
        return "role/" + name;
    }

}
