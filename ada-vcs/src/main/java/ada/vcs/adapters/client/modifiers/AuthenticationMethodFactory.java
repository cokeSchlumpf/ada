package ada.vcs.adapters.client.modifiers;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class AuthenticationMethodFactory {

    public AuthenticationMethod create(AuthenticationMethodMemento memento) {
        if (memento instanceof APIKeyAuthentication) {
            return ((APIKeyAuthentication) memento);
        } else if (memento instanceof StupidAuthenticationMemento) {
            return createStupid(((StupidAuthenticationMemento) memento));
        } else if (memento instanceof NoAuthentication) {
            return (NoAuthentication) memento;
        } else {
            throw new IllegalArgumentException("Unknown memento type");
        }
    }

    public AuthenticationMethod createAPIKey(String clientId, String key) {
        return APIKeyAuthentication.apply(clientId, key);
    }

    public AuthenticationMethod none() {
        return NoAuthentication.apply();
    }

    public StupidAuthentication createStupid(String username, Iterable<String> roles) {
        return StupidAuthentication.apply(username, roles);
    }

    public StupidAuthentication createStupid(String username, String... roles) {
        return StupidAuthentication.apply(username, roles);
    }

    public StupidAuthentication createStupid(StupidAuthenticationMemento memento) {
        return StupidAuthentication.apply(memento);
    }

}
