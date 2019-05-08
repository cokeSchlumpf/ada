package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.shared.converters.api.DataSink;

public interface Target extends Comparable<Target> {

    ResourceName alias();

    DataSink sink();

    Target withAlias(ResourceName alias);

    @Override
    default int compareTo(Target o) {
        return this.alias().getValue().compareTo(o.alias().getValue());
    }

}
