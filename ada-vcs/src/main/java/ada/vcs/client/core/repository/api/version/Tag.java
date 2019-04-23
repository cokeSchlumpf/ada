package ada.vcs.client.core.repository.api.version;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.User;

import java.util.Date;

public interface Tag extends Writable {

    User user();

    Date date();

    ResourceName alias();

    TagMemento memento();

}
