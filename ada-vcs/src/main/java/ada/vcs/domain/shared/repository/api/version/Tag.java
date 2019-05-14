package ada.vcs.domain.shared.repository.api.version;

import ada.commons.util.ResourceName;
import ada.commons.io.Writable;
import ada.vcs.domain.shared.repository.api.User;

import java.util.Date;

public interface Tag extends Writable {

    User user();

    Date date();

    ResourceName alias();

    TagMemento memento();

}
