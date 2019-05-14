package ada.vcs.domain.legacy.repository.api.version;

import ada.commons.util.ResourceName;
import ada.vcs.domain.legacy.repository.api.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

@AllArgsConstructor(staticName = "apply")
final class TagImpl implements Tag {

    private final ObjectMapper om;

    private final ResourceName alias;

    private final User user;

    private final Date date;

    @Override
    public User user() {
        return user;
    }

    @Override
    public Date date() {
        return date;
    }

    @Override
    public int hashCode() {
        return memento().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagImpl) {
            return memento().equals(((TagImpl) obj).memento());
        } else {
            return false;
        }
    }

    @Override
    public TagMemento memento() {
        return TagMemento.apply(alias, user, date);
    }

    @Override
    public ResourceName alias() {
        return alias;
    }

    @Override
    public String toString() {
        return memento().toString();
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, memento());
    }
}
