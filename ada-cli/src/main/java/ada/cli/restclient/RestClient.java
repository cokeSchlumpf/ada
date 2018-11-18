package ada.cli.restclient;

import org.reactivestreams.Publisher;

public interface RestClient {

    <T> T get(String uri, Class<T> cls);

    <T> Publisher<T> events(String uri, Class<T> cls);

}
