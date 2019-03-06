package ada.cli.restclient;

import com.ibm.ada.model.HttpEndpoint;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

@AllArgsConstructor(staticName = "apply")
public class RestClientSpringImpl implements RestClient {

    private final HttpEndpoint baseUrl;

    private final WebClientFactory webClientFactory;

    @Override
    public <T> Publisher<T> getAsPublisher(String uri, Class<T> cls) {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON)
            .method(HttpMethod.GET)
            .uri(baseUrl.resolve(uri).uri())
            .retrieve()
            .bodyToFlux(cls);
    }

    @Override
    public <T> T getAsType(String uri, Class<T> cls) throws Exception {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON)
            .method(HttpMethod.GET)
            .uri(baseUrl.resolve(uri).uri())
            .retrieve()
            .bodyToMono(cls)
            .toFuture()
            .get();
    }

    @Override
    public <T> Publisher<T> deleteAsPublisher(String uri, Class<T> cls) {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON)
            .method(HttpMethod.DELETE)
            .uri(baseUrl.resolve(uri).uri())
            .retrieve()
            .bodyToFlux(cls);
    }

    @Override
    public <T> T deleteAsType(String uri, Class<T> cls) throws Exception {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON)
            .method(HttpMethod.DELETE)
            .uri(baseUrl.resolve(uri).uri())
            .retrieve()
            .bodyToMono(cls)
            .toFuture()
            .get();
    }

    @Override
    public <T, B> Publisher<T> postAsPublisher(String uri, B body, Class<T> cls) {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.POST)
            .uri(baseUrl.resolve(uri).uri())
            .syncBody(body)
            .retrieve()
            .bodyToFlux(cls);
    }

    @Override
    public <T, B> Publisher<T> postAsPublisher(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls)  {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.POST)
            .uri(baseUrl.resolve(uri).uri())
            .body(body, bodyCls)
            .retrieve()
            .bodyToFlux(responseCls);
    }

    @Override
    public <T, B> T postAsType(String uri, B body, Class<T> cls) throws Exception {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.POST)
            .uri(baseUrl.resolve(uri).uri())
            .syncBody(body)
            .retrieve()
            .bodyToMono(cls)
            .toFuture()
            .get();
    }

    @Override
    public <T, B> T postAsType(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.POST)
            .uri(baseUrl.resolve(uri).uri())
            .body(body, bodyCls)
            .retrieve()
            .bodyToMono(responseCls)
            .toFuture()
            .get();
    }

    @Override
    public <T, B> Publisher<T> putAsPublisher(String uri, B body, Class<T> cls) {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.PUT)
            .uri(baseUrl.resolve(uri).uri())
            .syncBody(body)
            .retrieve()
            .bodyToFlux(cls);
    }

    @Override
    public <T, B> Publisher<T> putAsPublisher(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.PUT)
            .uri(baseUrl.resolve(uri).uri())
            .body(body, bodyCls)
            .retrieve()
            .bodyToFlux(responseCls);
    }

    @Override
    public <T, B> T putAsType(String uri, B body, Class<T> cls) throws Exception {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.PUT)
            .uri(baseUrl.resolve(uri).uri())
            .syncBody(body)
            .retrieve()
            .bodyToMono(cls)
            .toFuture()
            .get();
    }

    @Override
    public <T, B> T putAsType(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception {
        return webClientFactory
            .create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
            .method(HttpMethod.PUT)
            .uri(baseUrl.resolve(uri).uri())
            .body(body, bodyCls)
            .retrieve()
            .bodyToMono(responseCls)
            .toFuture()
            .get();
    }

}
