package ada.cli.restclient;

import org.reactivestreams.Publisher;

public interface RestClient {


    <T> Publisher<T> getAsPublisher(String uri, Class<T> cls) throws Exception;

    <T> T getAsType(String uri, Class<T> cls) throws Exception;


    <T> Publisher<T> deleteAsPublisher(String uri, Class<T> cls) throws Exception;

    <T> T deleteAsType(String uri, Class<T> cls) throws Exception;


    <T, B> Publisher<T> postAsPublisher(String uri, B body, Class<T> cls) throws Exception;

    <T, B> Publisher<T> postAsPublisher(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception;

    <T, B> T postAsType(String uri, B body, Class<T> cls) throws Exception;

    <T, B> T postAsType(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception;


    <T, B> Publisher<T> putAsPublisher(String uri, B body, Class<T> cls) throws Exception;

    <T, B> Publisher<T> putAsPublisher(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception;

    <T, B> T putAsType(String uri, B body, Class<T> cls) throws Exception;

    <T, B> T putAsType(String uri, Publisher<B> body, Class<B> bodyCls, Class<T> responseCls) throws Exception;

}
