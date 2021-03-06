package ada.adapters.cli.converters.api;

import ada.adapters.cli.converters.csv.CSVSourceMemento;
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
