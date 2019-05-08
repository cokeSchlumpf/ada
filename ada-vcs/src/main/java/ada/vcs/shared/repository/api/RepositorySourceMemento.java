package ada.vcs.shared.repository.api;

import ada.vcs.shared.repository.fs.FileSystemRepositorySourceMemento;
import ada.vcs.shared.repository.watcher.WatcherSourceMemento;
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
