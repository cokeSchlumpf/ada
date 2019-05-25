package ada.domain.legacy.repository.api.version;

import ada.domain.legacy.repository.api.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;

import java.util.Date;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VersionDetailsMemento {

    private final User user;

    private final Schema schema;

    private final Date date;

    private final String id;

    private final TagMemento tag;

    @JsonCreator
    public static VersionDetailsMemento apply(
        @JsonProperty("user") User user,
        @JsonProperty("schema") Schema schema,
        @JsonProperty("date") Date date,
        @JsonProperty("id") String id,
        @JsonProperty("tag") TagMemento tag) {

        return new VersionDetailsMemento(user, schema, date, id, tag);
    }

    public Optional<TagMemento> getTag() {
        return Optional.ofNullable(tag);
    }

}
