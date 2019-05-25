package ada.domain.legacy.converters.api;

import ada.domain.legacy.converters.avro.AvroSinkMemento;
import ada.domain.legacy.converters.csv.CSVSinkMemento;
import ada.domain.legacy.converters.local.LocalSinkMemento;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AvroSinkMemento.class, name = "avro"),
    @JsonSubTypes.Type(value = CSVSinkMemento.class, name = "csv"),
    @JsonSubTypes.Type(value = LocalSinkMemento.class, name = "local")
})
public interface DataSinkMemento {
}
