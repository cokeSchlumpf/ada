package ada.vcs.client;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.stream.javadsl.Source;
import org.junit.Test;

public class AkkaHttpUTest {

    @Test
    public void test() {
        HttpRequest.PUT("hallo")
            .withEntity(HttpEntities.create(ContentTypes.APPLICATION_OCTET_STREAM, ))
    }

}
