package ada.vcs.domain.legacy.repository.api;

import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySourceMemento;
import ada.vcs.domain.legacy.repository.watcher.WatcherSourceMemento;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FileSystemRepositorySourceMemento.class, name = "fs"),
    @JsonSubTypes.Type(value = WatcherSourceMemento.class, name = "watcher")
})
public interface RepositorySourceMemento {
}
