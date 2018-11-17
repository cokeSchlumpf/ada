package ada.web.controllers;

import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
import org.reactivestreams.Publisher;

/**
 * A controller which reveals information about the current application instance.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com).
 */
public interface AboutResource {

    /**
     * @return base information about the system.
     */
    AboutApplication getAbout();

    /**
     * Simple sample method to show how to stream long running processes/ responses to a ada.client.
     *
     * @return base information about the system as stream.
     */
    Publisher<String> getAboutStream();

    /**
     * @return information about the authenticated user.
     */
    AboutUser getUser();

}
