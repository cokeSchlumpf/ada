package ada.cli.restclient;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply")
public final class SpringResponseBodyImpl<T> implements RestClient.ResponseBody<T> {

    private final Function<MediaType, WebClient.ResponseSpec> responseFactory;

    private final Class<T> clazz;

    @Override
    public T await() {
        return responseFactory
            .apply(MediaType.APPLICATION_JSON)
            .bodyToMono(clazz)
            .block();
    }

    @Override
    public <E1 extends Throwable> T await(Class<E1> e1) throws E1 {
        try {
            return await();
        } catch (RuntimeException e) {
            throwIfPresent(e, e1);
            throw e;
        }
    }

    @Override
    public <E1 extends Throwable, E2 extends Throwable> T await(Class<E1> e1, Class<E2> e2) throws E1, E2 {
        try {
            return await();
        } catch (RuntimeException e) {
            throwIfPresent(e, e1);
            throwIfPresent(e, e2);
            throw e;
        }
    }

    @Override
    public <E1 extends Throwable, E2 extends Throwable, E3 extends Throwable>
    T await(Class<E1> e1, Class<E2> e2, Class<E3> e3) throws E1, E2, E3 {

        try {
            return await();
        } catch (RuntimeException e) {
            throwIfPresent(e, e1);
            throwIfPresent(e, e2);
            throwIfPresent(e, e3);
            throw e;
        }
    }

    @Override
    public CompletionStage<T> future() {
        return responseFactory
            .apply(MediaType.APPLICATION_JSON)
            .bodyToMono(clazz)
            .toFuture();
    }

    @Override
    public Publisher<T> publisher() {
        return responseFactory
            .apply(MediaType.APPLICATION_STREAM_JSON)
            .bodyToFlux(clazz);
    }

    @SuppressWarnings("unchecked")
    private <E extends Throwable> void throwIfPresent(Exception e, Class<E> cls) throws E {
        int i = ExceptionUtils.indexOfThrowable(e, cls);

        if (i > -1) {
            throw ((E) ExceptionUtils.getThrowableList(e).get(i));
        }
    }

}
