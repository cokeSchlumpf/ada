package ada.vcs.domain.shared.converters.api;

import ada.vcs.domain.shared.converters.avro.AvroSinkMemento;
import ada.vcs.domain.shared.converters.csv.CSVSinkMemento;
import ada.vcs.domain.shared.converters.local.LocalSinkMemento;
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
