package ada.web.api.resources.about.model;

import com.google.common.collect.ImmutableSet;
import lombok.*;

@Value
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class AuthenticatedUser implements User {

    private final String username;

    private final ImmutableSet<String> roles;

}
