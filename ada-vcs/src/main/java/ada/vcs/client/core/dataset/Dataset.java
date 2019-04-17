package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.DataSource;
import org.apache.avro.Schema;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Stream;

public interface Dataset extends Comparable<Dataset> {

    Stream<Target> getTargets();

    Dataset withAlias(ResourceName alias);

    Dataset withSource(DataSource<?> source);

    Dataset withSchema(Schema schema);

    Dataset withTargets(Map<String, Target> targets);

    ResourceName alias();

    DataSource<?> source();

    Schema schema();

    void writeTo(OutputStream os) throws IOException;

}
