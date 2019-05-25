package ada.adapters.client.modifiers;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;

public interface RequestModifier {

    Http modifyClient(Http http);

    HttpRequest modifyRequest(HttpRequest request);

}
