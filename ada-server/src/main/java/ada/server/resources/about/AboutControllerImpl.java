package ada.server.resources.about;

import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.User;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public class AboutControllerImpl {

    private final AboutResource aboutResource;

    private AboutControllerImpl(AboutResource aboutResource) {
        this.aboutResource = aboutResource;
    }

    static AboutControllerImpl create(AboutResource aboutResource) {
        return new AboutControllerImpl(aboutResource);
    }

    public Application getApplicationAsObject() {
        return aboutResource.getApplicationAsObject();
    }

    public Flux<ServerSentEvent<String>> getApplicationAsStream() {
        return Flux.from(aboutResource.getApplicationAsStream())
                   .map(s -> ServerSentEvent.builder(s)
                                            .build());
    }

    public User getUserAsObject(User user) {
        return aboutResource.getUser(user);
    }

    public Flux<ServerSentEvent<String>> getUserAsStream(User user) {
        return Flux
            .from(aboutResource.getUserAsStream(user))
            .map(s -> ServerSentEvent.builder(s).build());
    }

}
