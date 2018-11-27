package ada.cli.restclient;

import ada.cli.exceptions.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class RestClientImpl implements RestClient {

    private final URL baseUrl;

    private final ObjectMapper mapper;

    private RestClientImpl(URL baseUrl, ObjectMapper mapper) {
        this.baseUrl = baseUrl;
        this.mapper = mapper;
    }

    public static RestClientImpl apply(String baseUrl, ObjectMapper mapper) {
        try {
            return apply(new URL(baseUrl), mapper);
        } catch (MalformedURLException exception) {
            throw new RuntimeException(
                "Cannot create client due to wrong syntax of base URL",
                exception);
        }
    }

    private static RestClientImpl apply(URL baseUrl, ObjectMapper mapper) {
        return new RestClientImpl(baseUrl, mapper);
    }

    private String getURL(String uri) {
        try {
            return new URL(
                baseUrl,
                uri).toString();
        } catch (MalformedURLException e) {
            String s = String.format(
                "Cannot create URL for resource '%s'",
                uri);

            throw new ClientException(s, e);
        }
    }

    private WebClient.Builder getClientBuilder() {
        final Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(mapper, APPLICATION_JSON);
        final Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(mapper, APPLICATION_JSON);

        ExchangeStrategies strategies = ExchangeStrategies
            .builder()
            .codecs(config -> {
                config
                    .defaultCodecs()
                    .jackson2JsonEncoder(encoder);
                config
                    .defaultCodecs()
                    .jackson2JsonDecoder(decoder);
            })
            .build();

        return WebClient.builder().exchangeStrategies(strategies);
    }


    @Override
    public <T> T get(String uri, Class<T> cls) {
        WebClient client = getClientBuilder()
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
        WebClient client = getClientBuilder()
            .baseUrl(getURL(uri))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
            .build();

        ParameterizedTypeReference<ServerSentEvent<T>> type = new ParameterizedTypeReference<ServerSentEvent<T>>() {

            @Override
            public Type getType() {
                return ParameterizedTypeImpl.apply(
                    ServerSentEvent.class,
                    new Type[]{cls},
                    null);
            }

        };

        return client
            .method(HttpMethod.GET)
            .retrieve()
            .bodyToFlux(type)
            .map(ServerSentEvent::data);
    }

}
