package ada.vcs.server.domain.dvc.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticatedUser implements User {

    private final String id;

    private final String name;

    private final ImmutableSet<String> roles;

    @JsonCreator
    public static User apply(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("roles") Set<String> roles) {

        return new AuthenticatedUser(id, name, ImmutableSet.copyOf(roles));
    }

    public static User apply(String id, String name, String... roles) {
        return apply(id, name, Sets.newHashSet(roles));
    }

    @Override
    public UserId getUserId() {
        return UserId.apply(getId(), getDisplayName());
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    public Set<String> getRoles() {
        return roles;
    }

}
