package ada.vcs.server.domain.repository.valueobjects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = RoleAuthorization.class, name = "role"),
    @JsonSubTypes.Type(value = UserAuthorization.class, name = "user")
})
public interface Authorization {

    boolean hasAuthorization(User user);

}
