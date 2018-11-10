package ada.web.controllers.model;

import com.google.common.collect.ImmutableList;
import lombok.*;

/**
 * Data structure for sharing information about anonymous user.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com).
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class AboutAnonymousUser implements AboutUser {

    /**
     * See {@link AboutUser#getRoles()}.
     */
    public final ImmutableList<String> roles;

}
