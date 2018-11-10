package ada.web.resources.about;

import ada.web.resources.about.model.AboutApplication;
import ada.web.resources.about.model.AboutUser;
import org.reactivestreams.Publisher;

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
     * Simple sample method to show how to stream long running processes/ responses to a client.
     *
     * @return base information about the system as stream.
     */
    Publisher<String> getAboutStream();

    /**
     * @return information about the authenticated user.
     */
    AboutUser getUser();

}
