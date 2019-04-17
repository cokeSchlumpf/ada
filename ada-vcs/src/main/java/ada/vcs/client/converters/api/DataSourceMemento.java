package ada.vcs.client.converters.api;

import ada.vcs.client.converters.csv.CSVSource;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CSVSource.class, name = "csv")
})
public interface DataSourceMemento {
}
