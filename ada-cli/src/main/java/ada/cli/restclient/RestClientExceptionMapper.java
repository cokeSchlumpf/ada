package ada.cli.restclient;

import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

// TODO implement this decorators logic.
@AllArgsConstructor(staticName = "apply")
public class RestClientExceptionMapper implements RestClient {

    private final RestClient delegate;

    @Override
    public <T> Publisher<T> getAsPublisher(String uri, Class<T> cls) throws Exception {
        return delegate.getAsPublisher(uri, cls);
    }

    @Override
    public <T> T getAsType(String uri, Class<T> cls) throws Exception {
        return delegate.getAsType(uri, cls);
    }

    @Override
    public <T> Publisher<T> deleteAsPublisher(String uri, Class<T> cls) throws Exception {
        return delegate.deleteAsPublisher(uri, cls);
    }

    @Override
    public <T> T deleteAsType(String uri, Class<T> cls) throws Exception {
        return delegate.deleteAsType(uri, cls);
    }

    @Override
    public <T, B> Publisher<T> postAsPublisher(String uri, B body, Class<T> cls) throws Exception {
        return delegate.postAsPublisher(uri, body, cls);
    }

    @Override
    public <T, B> Publisher<T> postAsPublisher(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception {
        return delegate.postAsPublisher(uri, body, bodyCls, responseCls);
    }

    @Override
    public <T, B> T postAsType(String uri, B body, Class<T> cls) throws Exception {
        return delegate.postAsType(uri, body, cls);
    }

    @Override
    public <T, B> T postAsType(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception {
        return delegate.postAsType(uri, body, bodyCls, responseCls);
    }

    @Override
    public <T, B> Publisher<T> putAsPublisher(String uri, B body, Class<T> cls) throws Exception {
        return delegate.putAsPublisher(uri, body, cls);
    }

    @Override
    public <T, B> Publisher<T> putAsPublisher(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception {
        return delegate.putAsPublisher(uri, body, bodyCls, responseCls);
    }

    @Override
    public <T, B> T putAsType(String uri, B body, Class<T> cls) throws Exception {
        return delegate.putAsType(uri, body, cls);
    }

    @Override
    public <T, B> T putAsType(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception {
        return delegate.putAsType(uri, body, bodyCls, responseCls);
    }

}
