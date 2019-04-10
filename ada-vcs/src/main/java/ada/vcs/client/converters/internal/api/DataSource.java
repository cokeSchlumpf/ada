package ada.vcs.client.converters.internal.api;

import ada.vcs.client.converters.csv.CSVSource;
import akka.stream.Materializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.avro.Schema;

import java.util.concurrent.CompletionStage;

/**
 * A generic data source interface.
 *
 * @param <T> The type of the context the data source uses to store processing result information.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CSVSource.class, name = "csv")
})
public interface DataSource<T extends Context> {

    /**
     * Analyzes the data within the data source, e.g. to extract missing properties from the data source.
     *
     * @param schema A user provided schema for the data source.
     * @return An enriched instance of the data source which can be executed/ read.
     */
    CompletionStage<ReadableDataSource<T>> analyze(Materializer materializer, Schema schema);

    CompletionStage<ReadableDataSource<T>> analyze(Materializer materializer);

    @JsonIgnore
    String getInfo();

}
