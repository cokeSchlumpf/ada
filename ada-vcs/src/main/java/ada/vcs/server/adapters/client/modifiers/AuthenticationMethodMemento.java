package ada.vcs.server.adapters.client.modifiers;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = APIKeyAuthentication.class, name = "api"),
    @JsonSubTypes.Type(value = NoAuthentication.class, name = "none"),
    @JsonSubTypes.Type(value = StupidAuthenticationMemento.class, name = "stupid")
})
public interface AuthenticationMethodMemento {
}
