package ada.cli.restclient;

import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;

public interface RestClient {

    <T> ResponseBody<T> get(String uri, Class<T> responseType);

    <T> ResponseBody<T> delete(String uri, Class<T> responseType);

    <T, B> ResponseBody<T> delete(String uri, Class<T> responseType, B request);

    <T, B> ResponseBody<T> delete(String uri, Class<T> responseType, Publisher<B> request, Class<B> requestType);

    <T> ResponseBody<T> patch(String uri, Class<T> responseType);

    <T> ResponseBody<T> post(String uri, Class<T> responseType);

    <T, B> ResponseBody<T> post(String uri, Class<T> responseType, B request);

    <T, B> ResponseBody<T> post(String uri, Class<T> responseType, Publisher<B> request, Class<B> requestType);

    <T> ResponseBody<T> put(String uri, Class<T> responseType);

    <T, B> ResponseBody<T> put(String uri, Class<T> responseType, B request);

    <T, B> ResponseBody<T> put(String uri, Class<T> responseType, Publisher<B> request, Class<B> requestType);

    interface ResponseBody<T> {

        T await();

        <E1 extends Throwable> T await(Class<E1> e1) throws E1;

        <E1 extends Throwable, E2 extends Throwable> T await(Class<E1> e1, Class<E2> e2) throws E1, E2;

        <E1 extends Throwable, E2 extends Throwable, E3 extends Throwable>
        T await(Class<E1> e1, Class<E2> e2, Class<E3> e3) throws E1, E2, E3;

        CompletionStage<T> future();

        Publisher<T> publisher();

    }

}
