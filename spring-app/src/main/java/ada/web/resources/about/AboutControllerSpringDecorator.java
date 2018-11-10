package ada.web.resources.about;

import ada.web.resources.about.model.AboutAnonymousUser;
import ada.web.resources.about.model.AboutApplication;
import ada.web.resources.about.model.AboutUser;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@RestController
@RequestMapping("api/v1/about")
@SuppressWarnings("unused")
@Api(
    tags = "About",
    description = "Provides general information about the running Ada instance")
public class AboutControllerSpringDecorator implements AboutController {

    private final AboutController controller;

    @SuppressWarnings("unused")
    public AboutControllerSpringDecorator(AboutControllerConfiguration configuration) {
        this.controller = AboutControllerImpl.apply(
            configuration,
            AboutAnonymousUser.apply(ImmutableList.of()));
    }

    @Override
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(
        value = "Returns version information of Ada instance",
        notes = "The version information is added during build Process")
    public AboutApplication getAbout() {
        return controller.getAbout();
    }

    @Override
    @RequestMapping(
        path = "user",
        method = RequestMethod.GET)
    @ApiOperation(
        value = "Returns information about the authenticated user")
    public AboutUser getUser() {
        return controller.getUser();
    }

}
