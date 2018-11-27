package ada.cli.clients;

import ada.cli.restclient.RestClient;
import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.User;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Controller;

@Controller
public class AboutResourceClient implements AboutResource {

    private final RestClient client;

    public AboutResourceClient(RestClient client) {
        this.client = client;
    }

    public Application getApplicationAsObject() {
        return client.get("/api/v1/about", Application.class);
    }

    @Override
    public Publisher<String> getApplicationAsStream() {
        return client.events("/api/v1/about", String.class);
    }

    @Override
    public User getUser(User user) {
        return client.get("/api/v1/about/user", User.class);
    }

    @Override
    public Publisher<String> getUserAsStream(User user) {
        return client.events("/api/v1/about/user", String.class);
    }

}
