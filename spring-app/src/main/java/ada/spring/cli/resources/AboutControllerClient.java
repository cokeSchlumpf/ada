package ada.spring.cli.resources;

import ada.web.controllers.AboutResource;
import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
import org.reactivestreams.Publisher;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
public class AboutControllerClient implements AboutResource {

    @Override
    public AboutApplication getAbout() {
        return null;
    }

    @Override
    public Publisher<String> getAboutStream() {
        return null;
    }

    @Override
    public AboutUser getUser() {
        return null;
    }

}
