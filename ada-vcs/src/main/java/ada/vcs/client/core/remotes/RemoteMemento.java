package ada.vcs.client.core.remotes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FileSystemRemoteMemento.class, name = "fs"),
    @JsonSubTypes.Type(value = HttpRemoteMemento.class, name = "http")
})
public interface RemoteMemento {

}
