package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.DataSourceMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class DatasetMemento {

    private final ResourceName alias;

    private final DataSourceMemento source;

    private final Schema schema;

    private final List<TargetMemento> targets;

    @JsonCreator
    public static DatasetMemento apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("source") DataSourceMemento source,
        @JsonProperty("schema") Schema schema,
        @JsonProperty("targets") List<TargetMemento> targets) {

        return new DatasetMemento(alias, source, schema, targets);
    }

}
