package ada.vcs.server.adapters.client.modifiers;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
final class NoAuthentication implements AuthenticationMethod, AuthenticationMethodMemento {

    @Override
    public String info() {
        return "No authentication";
    }

    @Override
    public AuthenticationMethodMemento memento() {
        return this;
    }

    @Override
    public Http modifyClient(Http http) {
        return http;
    }

    @Override
    public HttpRequest modifyRequest(HttpRequest request) {
        return request;
    }
}
