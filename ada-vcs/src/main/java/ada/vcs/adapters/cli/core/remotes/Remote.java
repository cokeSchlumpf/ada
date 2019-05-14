package ada.vcs.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import ada.commons.io.Writable;
import ada.vcs.domain.legacy.repository.api.Repository;

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
