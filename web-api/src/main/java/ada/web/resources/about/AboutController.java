package ada.web.resources.about;

import ada.web.resources.about.model.AboutApplication;
import ada.web.resources.about.model.AboutUser;

import java.util.Map;

/**
 * A controller which reveals information about the current application instance.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com).
 */
public interface AboutController {

    /**
     * @return base information about the system.
     */
    AboutApplication getAbout();

    /**
     * @return information about the authenticated user.
     */
    AboutUser getUser();

}
