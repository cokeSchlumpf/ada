package ada.web.controllers.model;

import com.google.common.collect.ImmutableList;
import lombok.*;

/**
 * Data structure for authenticated users.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com).
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class AboutAuthenticatedUser implements AboutUser {

    /**
     * The name of the authenticated user.
     */
    public final String name;

    /**
     * See {@link AboutUser#getRoles()}.
     */
    public final ImmutableList<String> roles;

}
