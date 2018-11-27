package ada.web.api.resources.about.model;

import com.google.common.collect.ImmutableSet;
import lombok.*;

@Value
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class AnonymousUser implements User {

    private final ImmutableSet<String> roles;

    public static AnonymousUser apply() {
        return apply(ImmutableSet.of());
    }

}
