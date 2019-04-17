package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.DataSource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;
import org.apache.avro.Schema;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Value
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasetImpl implements Dataset {

    private final ResourceName alias;

    private final DataSource<?> source;

    private final Schema schema;

    private final Map<String, Target> targets;

    @JsonCreator
    public static Dataset apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("source") DataSource<?> source,
        @JsonProperty("schema") Schema schema,
        @JsonProperty("targets") List<Target> targets) {

        Map<String, Target> targetsMapped = Maps.newHashMap();
        targets.forEach(target -> targetsMapped.put(target.getAlias().getValue(), target));
        return new DatasetImpl(alias, source, schema, targetsMapped);
    }

    public static Dataset apply(ResourceName alias, DataSource<?> source, Schema schema) {
        return apply(alias, source, schema, Maps.newHashMap());
    }

    public static Dataset apply(ResourceName alias, DataSource<?> source, Schema schema, Map<String, Target> targets) {
        return new DatasetImpl(alias, source, schema, targets);
    }

    @Override
    public Stream<Target> getTargets() {
        return targets.values().stream();
    }

    @Override
    public int compareTo(Dataset o) {
        return this.getAlias().getValue().compareTo(o.getAlias().getValue());
    }

}
