package ada.domain.dvc.values.repository.version;

import ada.commons.util.ResourceName;
import ada.commons.io.Writable;
import ada.domain.dvc.values.repository.User;

import java.util.Date;

public interface Tag extends Writable {

    User user();

    Date date();

    ResourceName alias();

    TagMemento memento();

}
