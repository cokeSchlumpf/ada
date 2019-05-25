package ada.domain.dvc.values.repository;

import ada.domain.dvc.values.repository.fs.FileSystemRepositorySourceMemento;
import ada.domain.dvc.values.repository.watcher.WatcherSourceMemento;
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
