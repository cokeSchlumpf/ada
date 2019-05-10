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
public final class AnonymousUser implements User {

    private final ImmutableSet<String> roles;

    @JsonCreator
    public static User apply(
        @JsonProperty("roles") Set<String> roles) {

        return new AnonymousUser(ImmutableSet.copyOf(roles));
    }

    public static User apply(String... roles) {
        return apply(Sets.newHashSet(roles));
    }

}
