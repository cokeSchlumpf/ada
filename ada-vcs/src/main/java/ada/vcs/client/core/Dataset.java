package ada.vcs.client.core;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.internal.api.DataSource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.Value;
import org.apache.avro.Schema;

import java.util.Map;
import java.util.stream.Stream;

@Value
public class Dataset {

    private final ResourceName alias;

    private final DataSource<?> source;

    private final Schema schema;

    private final Map<String, Target> targets;

    @JsonCreator
    private Dataset(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("source") DataSource<?> source,
        @JsonProperty("schema") Schema schema,
        @JsonProperty("targets") Map<String, Target> targets) {

        this.alias = alias;
        this.source = source;
        this.schema = schema;
        this.targets = targets;
    }

    public static Dataset apply(ResourceName alias, DataSource<?> source, Schema schema) {
        return apply(alias, source, schema, Maps.newHashMap());
    }

    public static Dataset apply(ResourceName alias, DataSource<?> source, Schema schema, Map<String, Target> targets) {
        return new Dataset(alias, source, schema, targets);
    }

    public Stream<Target> getTargets() {
        return targets.values().stream();
    }

}
