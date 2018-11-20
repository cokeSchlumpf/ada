package ada.server.controllers;

import ada.server.web.impl.resources.about.AboutControllerConfiguration;
import ada.server.web.impl.resources.about.AboutResourceFactoryImpl;
import ada.web.controllers.AboutResource;
import ada.web.controllers.AboutResourceFactory;
import ada.web.controllers.model.AboutAnonymousUser;
import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
import akka.actor.ActorSystem;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@RestController
@RequestMapping("api/v1/about")
@Api(
    tags = "About",
    description = "Provides general information about the running Ada instance")
public class AboutResourceController {

    private final AboutResource controller;

    @SuppressWarnings("unused")
    public AboutResourceController(AboutControllerConfiguration configuration, ActorSystem system) {
        AboutResourceFactory f = AboutResourceFactoryImpl.apply(
            configuration,
            AboutAnonymousUser.apply(ImmutableList.of()),
            system);

        this.controller = f.create();
    }

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiOperation(
        value = "Returns version information of Ada instance",
        notes = "The version information is added during build Process")
    public AboutApplication getAbout() {
        return controller.getAbout();
    }

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_EVENT_STREAM_VALUE
        })
    @ApiOperation(
        value = "Returns version information of Ada instance",
        notes = "The version information is added during build Process")
    public Flux<ServerSentEvent<String>> getAboutEventStream() {
        return Flux
            .from(controller.getAboutStream())
            .map(s -> ServerSentEvent.builder(s).build());
    }

    @RequestMapping(
        path = "user",
        method = RequestMethod.GET)
    @ApiOperation(
        value = "Returns information about the authenticated user")
    public AboutUser getUser() {
        return controller.getUser();
    }

}
