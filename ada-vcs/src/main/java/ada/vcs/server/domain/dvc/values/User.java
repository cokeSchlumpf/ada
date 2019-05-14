package ada.vcs.server.domain.dvc.values;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AnonymousUser.class, name = "anonymous"),
    @JsonSubTypes.Type(value = AuthenticatedUser.class, name = "user")
})
public interface User {

    UserId getUserId();

    String getDisplayName();

    Set<String> getRoles();

}
