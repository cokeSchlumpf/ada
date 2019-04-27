package ada.vcs.client.core.repository.api.version;

import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.User;
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
