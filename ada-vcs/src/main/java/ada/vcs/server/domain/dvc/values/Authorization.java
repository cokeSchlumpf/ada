package ada.vcs.server.domain.dvc.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = RoleAuthorization.class, name = "role"),
    @JsonSubTypes.Type(value = UserAuthorization.class, name = "user"),
    @JsonSubTypes.Type(value = WildcardAuthorization.class, name = "wildcard")
})
public interface Authorization {

    @JsonIgnore
    boolean hasAuthorization(User user);

}
