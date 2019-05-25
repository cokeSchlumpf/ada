package ada.adapters.cli.core.dataset;

import ada.commons.util.ResourceName;
import ada.adapters.cli.converters.api.DataSourceMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;

import java.util.List;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class DatasetMemento {

    private static final String ALIAS = "alias";
    private static final String SOURCE = "source";
    private static final String REMOTE_SOURCE = "remote-source";
    private static final String SCHEMA = "schema";
    private static final String TARGETS = "targets";

    @JsonProperty(ALIAS)
    private final ResourceName alias;

    @JsonProperty(SOURCE)
    private final DataSourceMemento source;

    @JsonProperty(SCHEMA)
    private final Schema schema;

    @JsonProperty(TARGETS)
    private final List<TargetMemento> targets;

    @JsonProperty(REMOTE_SOURCE)
    private final RemoteSourceMemento remoteSource;

    @JsonCreator
    public static DatasetMemento apply(
        @JsonProperty(ALIAS) ResourceName alias,
        @JsonProperty(SOURCE) DataSourceMemento source,
        @JsonProperty(SCHEMA) Schema schema,
        @JsonProperty(TARGETS) List<TargetMemento> targets,
        @JsonProperty(REMOTE_SOURCE) RemoteSourceMemento remoteSource) {

        return new DatasetMemento(alias, source, schema, targets, remoteSource);
    }

    public Optional<RemoteSourceMemento> getRemoteSource() {
        return Optional.ofNullable(remoteSource);
    }

}
