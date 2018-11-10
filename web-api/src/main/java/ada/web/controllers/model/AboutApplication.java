package ada.web.controllers.model;

import lombok.*;

/**
 * Data structure for sharing information about the application.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com).
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class AboutApplication {

    /**
     * The name of the application instance.
     */
    public final String name;

    /**
     * The build number which is injected during the actual build.
     */
    public final String build;

}
