package ada.adapters.cli.core.dataset;

import ada.commons.util.ResourceName;
import ada.domain.legacy.converters.api.DataSink;

public interface Target extends Comparable<Target> {

    ResourceName alias();

    DataSink sink();

    Target withAlias(ResourceName alias);

    @Override
    default int compareTo(Target o) {
        return this.alias().getValue().compareTo(o.alias().getValue());
    }

}
