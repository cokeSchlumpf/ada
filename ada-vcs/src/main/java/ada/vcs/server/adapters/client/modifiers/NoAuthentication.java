package ada.vcs.server.adapters.client.modifiers;

import ada.commons.util.ResourceName;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
final class NoAuthentication implements AuthenticationMethod, AuthenticationMethodMemento {

    @Override
    public String info() {
        return "No authentication";
    }

    @Override
    @JsonIgnore
    public Optional<ResourceName> getDefaultNamespace() {
        return Optional.empty();
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
