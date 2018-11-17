package ada.server.controllers;

import ada.server.web.impl.controllers.AboutControllerConfiguration;
import ada.web.controllers.AboutResource;
import ada.web.controllers.AboutResourceFactory;
import ada.server.web.impl.controllers.AboutResourceFactoryImpl;
import ada.web.controllers.model.AboutAnonymousUser;
import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@RestController
@RequestMapping("api/v1/about")
@Api(
    tags = "About",
    description = "Provides general information about the running Ada instance")
public class AboutResourceController {

    private static final Logger LOG = LoggerFactory.getLogger(AboutResourceController.class);

    private final AboutResource controller;

    private final Materializer materializer;

    @SuppressWarnings("unused")
    public AboutResourceController(AboutControllerConfiguration configuration, ActorSystem system) {
        AboutResourceFactory f = AboutResourceFactoryImpl.apply(
            configuration,
            AboutAnonymousUser.apply(ImmutableList.of()),
            system);

        this.controller = f.create();
        this.materializer = ActorMaterializer.create(system);
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
                LOG.debug("Streaming response failed", exception);
                emitter.completeWithError(exception);
            } else {
                emitter.complete();
            }
        });

        return emitter;
    }

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_EVENT_STREAM_VALUE
        })
    @ApiOperation(
        value = "Returns version information of Ada instance",
        notes = "The version information is added during build Process")
    public SseEmitter getAboutEventStream() {
        SseEmitter emitter = new SseEmitter();

        CompletionStage<Done> done = Source
            .fromPublisher(controller.getAboutStream())
            .runWith(
                Sink.foreach(s -> {
                    SseEmitter.SseEventBuilder builder = SseEmitter
                        .event()
                        .data(s, MediaType.TEXT_PLAIN)
                        .name("text");

                    emitter.send(builder);
                }),
                this.materializer);

        done.whenComplete((result, exception) -> {
            LOG.info("Finished streaming response");

            if (!Objects.isNull(exception)) {
                LOG.debug("Streaming response failed", exception);
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
