package ada.web.controllers;

import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
import org.reactivestreams.Publisher;

/**
 * Implementation of About controller of the application.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
public class AboutResourceImplJava implements AboutResource {

    private final AboutControllerConfiguration configuration;

    private final AboutUser user;

    private AboutResourceImplJava(AboutControllerConfiguration configuration, AboutUser user) {
        this.configuration = configuration;
        this.user = user;
    }

    public static AboutResourceImplJava apply(AboutControllerConfiguration configuration, AboutUser user) {
        return new AboutResourceImplJava(configuration, user);
    }

    @Override
    public AboutApplication getAbout() {
        return AboutApplication.apply(configuration.getName(), configuration.getBuild());
    }

    @Override
    public Publisher<String> getAboutStream() {
        return null;
    }

    @Override
    public AboutUser getUser() {
        return user;
    }

}
