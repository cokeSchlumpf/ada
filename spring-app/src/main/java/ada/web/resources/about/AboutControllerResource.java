package ada.web.resources.about;

import ada.web.resources.about.model.AboutAnonymousUser;
import ada.web.resources.about.model.AboutApplication;
import ada.web.resources.about.model.AboutUser;
import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@RestController
@RequestMapping("api/v1/about")
@SuppressWarnings("unused")
@Api(
    tags = "About",
    description = "Provides general information about the running Ada instance")
public class AboutControllerResource {

    private static final Logger LOG = LoggerFactory.getLogger(AboutControllerResource.class);

    private final AboutController controller;

    private final Materializer materializer;

    @SuppressWarnings("unused")
    public AboutControllerResource(AboutControllerConfiguration configuration, ActorSystem system) {
        AboutControllerFactory f = AboutControllerFactoryImpl.apply(
            configuration,
            AboutAnonymousUser.apply(ImmutableList.of()),
            system);

        this.controller = f.create();
        this.materializer = ActorMaterializer.create(system);
    }

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
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
            MediaType.TEXT_PLAIN_VALUE
        })
    @ApiOperation(
        value = "Returns version information of Ada instance",
        notes = "The version information is added during build Process")
    public ResponseBodyEmitter getAboutStream() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        CompletionStage<Done> done = Source
            .fromPublisher(controller.getAboutStream())
            .runWith(
                Sink.foreach(s -> emitter.send(s, MediaType.TEXT_PLAIN)),
                this.materializer);

        done.whenComplete((result, exception) -> {
            LOG.info("Finished streaming response");

            if (!Objects.isNull(exception)) {
                LOG.error("Streaming response failed", exception);
                emitter.completeWithError(exception);
            } else {
                emitter.complete();
            }
        });

        return emitter;
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
