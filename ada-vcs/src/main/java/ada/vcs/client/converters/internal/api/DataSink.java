package ada.vcs.client.converters.internal.api;

import ada.vcs.client.converters.csv.CSVSink;
import akka.stream.javadsl.Sink;
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
    @JsonSubTypes.Type(value = CSVSink.class, name = "csv")
})
public interface DataSink {

    Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema);

}
