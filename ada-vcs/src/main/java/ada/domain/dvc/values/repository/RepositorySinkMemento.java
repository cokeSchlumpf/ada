package ada.domain.dvc.values.repository;

import ada.domain.dvc.values.repository.fs.FileSystemRepositorySinkMemento;
import ada.domain.dvc.values.repository.watcher.WatcherSinkMemento;
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
