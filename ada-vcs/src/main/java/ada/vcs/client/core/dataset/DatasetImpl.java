package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.shared.converters.api.DataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.apache.avro.Schema;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class DatasetImpl implements Dataset {

    private final ObjectMapper om;

    private final ResourceName alias;

    private final DataSource source;

    private final Schema schema;

    private final Map<String, Target> targets;

    private final RemoteSource remoteSource;

    public static Dataset apply(
        ObjectMapper om, ResourceName alias, DataSource source, Schema schema, List<Target> targets) {

        return apply(om, alias, source, schema, targets, null);
    }

    public static Dataset apply(
        ObjectMapper om, ResourceName alias, DataSource source, Schema schema, List<Target> targets, RemoteSource remoteSource) {

        Map<String, Target> targetsMapped = Maps.newHashMap();
        targets.forEach(target -> targetsMapped.put(target.alias().getValue(), target));
        return new DatasetImpl(om, alias, source, schema, targetsMapped, remoteSource);
    }

    public static Dataset apply(ObjectMapper om, ResourceName alias, DataSource source, Schema schema, Map<String, Target> targets) {
        return apply(om, alias, source, schema, targets, null);
    }

    public static Dataset apply(
        ObjectMapper om, ResourceName alias, DataSource source,
        Schema schema, Map<String, Target> targets, RemoteSource remoteSource) {

        return new DatasetImpl(om, alias, source, schema, targets,remoteSource);
    }

    public static Dataset apply(
        ObjectMapper om, ResourceName alias, DataSource source, Schema schema) {
        return apply(om, alias, source, schema, Maps.newHashMap());
    }

    @Override
    public Stream<Target> getTargets() {
        return targets.values().stream();
    }

    @Override
    public ResourceName alias() {
        return alias;
    }

    @Override
    public DataSource source() {
        return source;
    }

    @Override
    public Optional<RemoteSource> remoteSource() {
        return Optional.ofNullable(remoteSource);
    }

    @Override
    public Schema schema() {
        return schema;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        List<TargetMemento> targets = this.targets
            .values()
            .stream()
            .map(t -> TargetMemento.apply(t.alias(), t.sink().memento()))
            .collect(Collectors.toList());

        om.writeValue(os, DatasetMemento.apply(
            alias, source.memento(),
            schema, targets, remoteSource().map(RemoteSource::memento).orElse(null)));
    }

    @Override
    public int compareTo(Dataset o) {
        return this.alias().getValue().compareTo(o.alias().getValue());
    }

}
