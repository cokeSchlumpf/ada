package ada.cli.restclient;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
public class RestClientImpl implements RestClient {

    private final URL baseUrl;

    public static RestClientImpl apply(String baseUrl) {
        try {
            return apply(new URL(baseUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot create client due to wrong syntax of base URL", e);
        }
    }

    private String getURL(String uri) {
        try {
            return new URL(baseUrl, uri).toString();
        } catch (MalformedURLException e) {
            String s = String.format("Cannot create URL for resource '%s'", uri);
            throw new RuntimeException(s, e);
        }
    }

    @Override
    public <T> T get(String uri, Class<T> cls) {
        WebClient client = WebClient
            .builder()
            .baseUrl(getURL(uri))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

        return client
            .method(HttpMethod.GET)
            .retrieve()
            .bodyToMono(cls)
            .block();
    }

    @Override
    public <T> Publisher<T> events(String uri, Class<T> cls) {
        WebClient client = WebClient
            .builder()
            .baseUrl(getURL(uri))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
            .build();

        ParameterizedTypeReference<ServerSentEvent<T>> type = new ParameterizedTypeReference<ServerSentEvent<T>>() {

            @Override
            public Type getType() {
                return new ParameterizedTypeImpl(
                    ServerSentEvent.class,
                    new Type[] { cls },
                    null
                );
            }

        };

        return client
            .method(HttpMethod.GET)
            .retrieve()
            .bodyToFlux(type)
            .map(ServerSentEvent::data);
    }

}
