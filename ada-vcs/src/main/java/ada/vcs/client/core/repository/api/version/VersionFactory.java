package ada.vcs.client.core.repository.api.version;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@AllArgsConstructor(staticName = "apply")
public final class VersionFactory {

    private final ObjectMapper om;

    public VersionDetails createDetails(User user, String message, Schema schema) {
        Date date = new Date();

        String id = Hashing
            .goodFastHash(128)
            .newHasher()
            .putString(user.getName(), StandardCharsets.UTF_8)
            .putString(user.getEmail().orElse(""), StandardCharsets.UTF_8)
            .putString(date.toString(), StandardCharsets.UTF_8)
            .putString(message, StandardCharsets.UTF_8)
            .hash()
            .toString();

        return VersionDetailsImpl.apply(om, user, message, schema, date, id, null);
    }

    public VersionDetails createDetails(VersionDetailsMemento memento) {
        return VersionDetailsImpl.apply(
            om,
            memento.getUser(),
            memento.getMessage(),
            memento.getSchema(),
            memento.getDate(),
            memento.getId(),
            memento.getTag().map(this::createTag).orElse(null));
    }

    public VersionDetails createDetails(InputStream is) throws IOException {
        VersionDetailsMemento memento = om.readValue(is, VersionDetailsMemento.class);
        return createDetails(memento);
    }

    public Tag createTag(ResourceName alias, User user) {
        return TagImpl.apply(om, alias, user, new Date());
    }

    public Tag createTag(TagMemento memento) {
        return TagImpl.apply(om, memento.getAlias(), memento.getUser(), memento.getDate());
    }

    public Tag createTag(InputStream is) throws IOException {
        TagMemento memento = om.readValue(is, TagMemento.class);
        return createTag(memento);
    }

}
