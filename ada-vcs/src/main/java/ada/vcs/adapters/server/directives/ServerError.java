package ada.vcs.adapters.server.directives;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ErrorMessageServerError.class, name = "user-error"),
    @JsonSubTypes.Type(value = InternalServerError.class, name = "server-error")
})
public interface ServerError {

    String getMessage();

}
