package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.DataSource;
import ada.vcs.client.core.Writable;
import org.apache.avro.Schema;

import java.util.Map;
import java.util.stream.Stream;

public interface Dataset extends Comparable<Dataset>, Writable {

    Stream<Target> getTargets();

    Dataset withAlias(ResourceName alias);

    Dataset withSource(DataSource<?> source);

    Dataset withSchema(Schema schema);

    Dataset withTargets(Map<String, Target> targets);

    ResourceName alias();

    DataSource<?> source();

    Schema schema();

}
