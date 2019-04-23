package ada.vcs.client.core.repository.api.version;

import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.User;
import org.apache.avro.Schema;

import java.util.Date;
import java.util.Optional;

public interface VersionDetails extends Writable, Comparable<VersionDetails> {

    User user();

    String message();

    Date date();

    String id();

    Schema schema();

    Optional<Tag> tag();

    VersionDetails withTag(Tag tag);

}
