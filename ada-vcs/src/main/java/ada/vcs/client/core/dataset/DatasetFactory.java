package ada.vcs.client.core.dataset;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.DataSink;
import ada.vcs.client.converters.api.DataSinkFactory;
import ada.vcs.client.converters.api.DataSource;
import ada.vcs.client.converters.api.DataSourceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply")
public final class DatasetFactory {

    private final ObjectMapper om;

    private final DataSourceFactory dataSourceFactory;

    private final DataSinkFactory dataSinkFactory;

    public static DatasetFactory apply() {
        return apply(ObjectMapperFactory.apply().create(true), DataSourceFactory.apply(), DataSinkFactory.apply());
    }

    public Dataset createDataset(
        ResourceName alias, DataSource<?> source, Schema schema, List<TargetImpl> targets) {

        Map<String, Target> targetsMapped = targets
            .stream()
            .collect(Collectors.toMap(t -> t.alias().getValue(), t -> t));

        return DatasetImpl.apply(om, alias, source, schema, targetsMapped);
    }

    public Dataset createDataset(DatasetMemento memento) {
        DataSource<?> source = dataSourceFactory.createDataSource(memento.getSource());
        List<Target> targets = memento
            .getTargets()
            .stream()
            .map(m -> TargetImpl.apply(m.getAlias(), dataSinkFactory.createDataSink(m.getSink())))
            .collect(Collectors.toList());

        return DatasetImpl.apply(om, memento.getAlias(), source,memento.getSchema(), targets);
    }

    public Dataset createDataset(ResourceName alias, DataSource<?> source, Schema schema) {
        return createDataset(alias, source, schema, Maps.newHashMap());
    }

    public Dataset createDataset(ResourceName alias, DataSource<?> source, Schema schema, Map<String, Target> targets) {
        return DatasetImpl.apply(om, alias, source, schema, targets);
    }

    public Dataset createDataset(InputStream is) throws IOException {
        return createDataset(om.readValue(is, DatasetMemento.class));
    }

    public Target createTarget(ResourceName alias, DataSink sink) {
        return TargetImpl.apply(alias, sink);
    }

}
