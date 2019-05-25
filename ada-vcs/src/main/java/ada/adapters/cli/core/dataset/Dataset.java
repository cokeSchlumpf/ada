package ada.adapters.cli.core.dataset;

import ada.commons.util.ResourceName;
import ada.domain.legacy.converters.api.DataSource;
import ada.commons.io.Writable;
import org.apache.avro.Schema;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface Dataset extends Comparable<Dataset>, Writable {

    Stream<Target> getTargets();

    Dataset withAlias(ResourceName alias);

    Dataset withRemoteSource(RemoteSource source);

    Dataset withSource(DataSource source);

    Dataset withSchema(Schema schema);

    Dataset withTargets(Map<String, Target> targets);

    ResourceName alias();

    DataSource source();

    Optional<RemoteSource> remoteSource();

    Schema schema();

}
