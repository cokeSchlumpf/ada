package ada.vcs.shared.converters.api;

import ada.vcs.shared.converters.avro.AvroSinkMemento;
import ada.vcs.shared.converters.csv.CSVSinkMemento;
import ada.vcs.shared.converters.local.LocalSinkMemento;
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
