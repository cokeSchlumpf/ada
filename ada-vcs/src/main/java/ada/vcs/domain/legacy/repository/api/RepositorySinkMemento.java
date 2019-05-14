package ada.vcs.domain.legacy.repository.api;

import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySinkMemento;
import ada.vcs.domain.legacy.repository.watcher.WatcherSinkMemento;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FileSystemRepositorySinkMemento.class, name = "fs"),
    @JsonSubTypes.Type(value = WatcherSinkMemento.class, name = "watcher")
})
public interface RepositorySinkMemento {



}
