package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.internal.api.WriteSummary;
import ada.vcs.client.core.FileSystemDependent;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

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

    Sink<GenericRecord, CompletionStage<WriteSummary>> push(Schema schema);

    @Override
    default int compareTo(Remote o) {
        return this.getAlias().getValue().compareTo(o.getAlias().getValue());
    }

}
