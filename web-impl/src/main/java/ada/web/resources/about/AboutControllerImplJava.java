package ada.web.resources.about;

import ada.web.resources.about.model.AboutApplication;
import ada.web.resources.about.model.AboutUser;
import org.reactivestreams.Publisher;

/**
 * Implementation of About controller of the application.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
public class AboutControllerImplJava implements AboutController {

    private final AboutControllerConfiguration configuration;

    private final AboutUser user;

    private AboutControllerImplJava(AboutControllerConfiguration configuration, AboutUser user) {
        this.configuration = configuration;
        this.user = user;
    }

    public static AboutControllerImplJava apply(AboutControllerConfiguration configuration, AboutUser user) {
        return new AboutControllerImplJava(configuration, user);
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
