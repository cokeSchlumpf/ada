package ada.vcs.client.core.dataset;

import java.util.stream.Stream;

public interface Dataset extends Comparable<Dataset> {

    Stream<Target> getTargets();

    Dataset withAlias(ada.commons.util.ResourceName alias);

    Dataset withSource(ada.vcs.client.converters.api.DataSource<?> source);

    Dataset withSchema(org.apache.avro.Schema schema);

    Dataset withTargets(java.util.Map<String, Target> targets);

    ada.commons.util.ResourceName getAlias();

    ada.vcs.client.converters.api.DataSource<?> getSource();

    org.apache.avro.Schema getSchema();

}
