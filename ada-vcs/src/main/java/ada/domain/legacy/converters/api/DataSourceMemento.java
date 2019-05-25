package ada.domain.legacy.converters.api;

import ada.domain.legacy.converters.csv.CSVSourceMemento;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CSVSourceMemento.class, name = "csv")
})
public interface DataSourceMemento {
}
