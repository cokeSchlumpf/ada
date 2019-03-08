package ada.cli.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.model.HttpEndpoint;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON;

// TODO: Exception Mapping
@AllArgsConstructor(staticName = "apply")
public final class SpringRestClientImpl implements RestClient {

    private final HttpEndpoint baseUrl;

    private final ObjectMapper om;

    @Override
    public <T> ResponseBody<T> get(String uri, Class<T> responseType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType)
                .method(HttpMethod.GET)
                .uri(baseUrl.resolve(uri).uri())
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T> ResponseBody<T> delete(String uri, Class<T> responseType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType)
                .method(HttpMethod.DELETE)
                .uri(baseUrl.resolve(uri).uri())
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T, B> ResponseBody<T> delete(String uri, Class<T> responseType, B request) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType, MediaType.APPLICATION_JSON)
                .method(HttpMethod.DELETE)
                .uri(baseUrl.resolve(uri).uri())
                .syncBody(request)
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T, B> ResponseBody<T> delete(String uri, Class<T> responseType, Publisher<B> request, Class<B> requestType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType, MediaType.APPLICATION_STREAM_JSON)
                .method(HttpMethod.DELETE)
                .uri(baseUrl.resolve(uri).uri())
                .body(request, requestType)
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T> ResponseBody<T> patch(String uri, Class<T> responseType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType)
                .method(HttpMethod.PATCH)
                .uri(baseUrl.resolve(uri).uri())
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T> ResponseBody<T> post(String uri, Class<T> responseType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType)
                .method(HttpMethod.POST)
                .uri(baseUrl.resolve(uri).uri())
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T, B> ResponseBody<T> post(String uri, Class<T> responseType, B request) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType, MediaType.APPLICATION_JSON)
                .method(HttpMethod.POST)
                .uri(baseUrl.resolve(uri).uri())
                .syncBody(request)
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T, B> ResponseBody<T> post(String uri, Class<T> responseType, Publisher<B> request, Class<B> requestType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType, MediaType.APPLICATION_JSON)
                .method(HttpMethod.POST)
                .uri(baseUrl.resolve(uri).uri())
                .body(request, requestType)
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T> ResponseBody<T> put(String uri, Class<T> responseType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType)
                .method(HttpMethod.PUT)
                .uri(baseUrl.resolve(uri).uri())
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T, B> ResponseBody<T> put(String uri, Class<T> responseType, B request) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType, MediaType.APPLICATION_JSON)
                .method(HttpMethod.PUT)
                .uri(baseUrl.resolve(uri).uri())
                .syncBody(request)
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    @Override
    public <T, B> ResponseBody<T> put(String uri, Class<T> responseType, Publisher<B> request, Class<B> requestType) {
        Function<MediaType, WebClient.ResponseSpec> response = acceptMediaType ->
            webClient(acceptMediaType, MediaType.APPLICATION_JSON)
                .method(HttpMethod.PUT)
                .uri(baseUrl.resolve(uri).uri())
                .body(request, requestType)
                .retrieve();

        return SpringResponseBodyImpl.apply(response, responseType);
    }

    private WebClient webClient(MediaType accept, MediaType contentType) {
        WebClient.Builder builder = WebClient
            .builder()
            .exchangeStrategies(exchangeStrategies())
            .defaultHeader(HttpHeaders.ACCEPT, accept.toString());

        if (contentType != null) {
            builder.defaultHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
        }

        return builder.build();
    }

    private WebClient webClient(MediaType accept) {
        return webClient(accept, null);
    }

    private ExchangeStrategies exchangeStrategies() {
        Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(om, APPLICATION_JSON);
        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(om, APPLICATION_JSON);

        return ExchangeStrategies
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
    }

}
