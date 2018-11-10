package ada.web.resources.about;

import ada.web.resources.about.model.AboutApplication;
import ada.web.resources.about.model.AboutUser;

/**
 * Implementation of About controller of the application.
 *
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
public class AboutControllerImpl implements AboutController {

    private final AboutControllerConfiguration configuration;

    private final AboutUser user;

    private AboutControllerImpl(AboutControllerConfiguration configuration, AboutUser user) {
        this.configuration = configuration;
        this.user = user;
    }

    public static AboutControllerImpl apply(AboutControllerConfiguration configuration, AboutUser user) {
        return new AboutControllerImpl(configuration, user);
    }

    @Override
    public AboutApplication getAbout() {
        return AboutApplication.apply(configuration.getName(), configuration.getBuild());
    }

    @Override
    public AboutUser getUser() {
        return user;
    }

}
