package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.avro.AvroSink;
import ada.vcs.client.converters.csv.CSVSink;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FileSystemRemote.class, name = "fs"),
    @JsonSubTypes.Type(value = HttpRemote.class, name = "http")
})
public interface Remote extends Comparable<Remote> {

    ResourceName getAlias();

    @JsonIgnore
    String getInfo();

    @Override
    default int compareTo(Remote o) {
        return this.getAlias().getValue().compareTo(o.getAlias().getValue());
    }

}
