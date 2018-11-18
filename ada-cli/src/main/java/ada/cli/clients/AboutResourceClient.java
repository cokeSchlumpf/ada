package ada.cli.clients;

import ada.cli.restclient.RestClient;
import ada.web.controllers.AboutResource;
import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Controller;

@Controller
public class AboutResourceClient implements AboutResource {

    private final RestClient client;

    public AboutResourceClient(RestClient client) {
        this.client = client;
    }

    @Override
    public AboutApplication getAbout() {
        return client.get("/api/v1/about", AboutApplication.class);
    }

    @Override
    public Publisher<String> getAboutStream() {
        return client.events("/api/v1/about", String.class);
    }

    @Override
    public AboutUser getUser() {
        return client.get("/api/v1/about/user", AboutUser.class);
    }

}
