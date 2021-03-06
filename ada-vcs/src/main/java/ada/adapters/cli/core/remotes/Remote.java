package ada.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import ada.commons.io.Writable;
import ada.adapters.cli.repository.api.Repository;

public interface Remote
    extends Comparable<Remote>, Writable, Repository {

    ResourceName alias();

    String info();

    RemoteMemento memento();

    Remote withAlias(ResourceName alias);

    @Override
    default int compareTo(Remote o) {
        return this.alias().getValue().compareTo(o.alias().getValue());
    }

}
