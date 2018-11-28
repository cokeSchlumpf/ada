package ada.server.resources.about;

import ada.server.resources.AuthenticatedResource;
import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

@Api("About")
@RestController
@RequestMapping("api/v1/about")
public class AboutControllerAnnotations implements AuthenticatedResource {

    private final AboutControllerImpl impl;

    public AboutControllerAnnotations(AboutResource aboutResource) {
        this.impl = AboutControllerImpl.create(aboutResource);
    }

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
        value = "Returns version information of Ada instance",
        notes = "The version information is added during build Process")
    public Application getApplicationAsObject() {
        return impl.getApplicationAsObject();
    }

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @ApiOperation(
        value = "Returns version information of Ada instance provided as string stream for printing",
        notes = "The version information is added during build Process")
    public Flux<ServerSentEvent<String>> getApplicationAsStream() {
        return impl.getApplicationAsStream();
    }

    @RequestMapping(
        method = RequestMethod.GET,
        path = "/user",
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
        value = "Returns information about the authenticated user",
        notes = "The user is authenticated when sending a request to Ada")
    public User getUserAsObject(ServerWebExchange exchange) {
        return impl.getUserAsObject(getUser(exchange));
    }

    @RequestMapping(
        method = RequestMethod.GET,
        path = "/user",
        produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @ApiOperation(
        value = "Returns information about the authenticated user provided as string stream for printing",
        notes = "The user is authenticated when sending a request to Ada")
    public Flux<ServerSentEvent<String>> getUserAsStream(ServerWebExchange exchange) {
        return impl.getUserAsStream(getUser(exchange));
    }

}
