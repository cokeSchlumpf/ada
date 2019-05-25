package ada.domain.dvc.values.repository.version;

import ada.domain.dvc.values.repository.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
final class VersionDetailsImpl implements VersionDetails {

    private final ObjectMapper om;

    private final User user;

    private final Schema schema;

    private final Date date;

    private final String id;

    private final Tag tag;

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, memento());
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public VersionDetailsMemento memento() {
        return VersionDetailsMemento.apply(
            user, schema, date, id,
            tag().map(Tag::memento).orElse(null));
    }

    @Override
    public Date date() {
        return date;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Schema schema() {
        return schema;
    }

    @Override
    public Optional<Tag> tag() {
        return Optional.ofNullable(tag);
    }

    @Override
    public VersionDetails withTag(Tag tag) {
        return apply(om, user, schema, date, id, tag);
    }

    @Override
    public int hashCode() {
        return memento().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VersionDetailsImpl) {
            return memento().equals(((VersionDetailsImpl) other).memento());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return memento().toString();
    }

    @Override
    public int compareTo(VersionDetails o) {
        return date.compareTo(o.date());
    }

}
