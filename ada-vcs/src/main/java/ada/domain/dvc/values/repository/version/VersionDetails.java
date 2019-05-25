package ada.domain.dvc.values.repository.version;

import ada.commons.io.Writable;
import ada.domain.dvc.values.repository.User;
import org.apache.avro.Schema;

import java.util.Date;
import java.util.Optional;

public interface VersionDetails extends Writable, Comparable<VersionDetails> {

    User user();

    Date date();

    String id();

    VersionDetailsMemento memento();

    Schema schema();

    Optional<Tag> tag();

    VersionDetails withTag(Tag tag);

}
