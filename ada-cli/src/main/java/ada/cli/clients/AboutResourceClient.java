package ada.cli.clients;

import ada.web.controllers.AboutResource;
import ada.web.controllers.model.AboutAnonymousUser;
import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
import akka.actor.ActorRef;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

public class AboutResourceClient implements AboutResource {

    private final ActorMaterializer mat;

    private AboutResourceClient(ActorMaterializer mat) {
        this.mat = mat;
    }

    public static AboutResourceClient apply(ActorMaterializer mat) {
        return new AboutResourceClient(mat);
    }

    @Override
    public AboutApplication getAbout() {
        return AboutApplication.apply("foo", "bar");
    }

    @Override
    public Publisher<String> getAboutStream() {
        WebClient client = WebClient
            .builder()
            .baseUrl("http://localhost:8080")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
            .build();

        ParameterizedTypeReference<ServerSentEvent<String>> type = new ParameterizedTypeReference<ServerSentEvent<String>>() {
        };

        Flux<ServerSentEvent<String>> eventStream = client
            .method(HttpMethod.GET)
            .uri("/api/v1/about")
            .retrieve()
            .bodyToFlux(type);

        return eventStream.map(ServerSentEvent::data);
    }

    @Override
    public AboutUser getUser() {
        return AboutAnonymousUser.apply(ImmutableList.of());
    }

}
