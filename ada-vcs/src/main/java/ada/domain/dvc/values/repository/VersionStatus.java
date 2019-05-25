package ada.domain.dvc.values.repository;

import ada.domain.dvc.protocol.api.ValueObject;
import ada.domain.dvc.values.repository.version.VersionDetails;
import ada.domain.dvc.values.repository.version.VersionDetailsMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Date;

@Value
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VersionStatus implements ValueObject {

    private VersionDetailsMemento details;

    private VersionState state;

    private Date updated;

    @JsonCreator
    public static VersionStatus apply(
        @JsonProperty("details") VersionDetailsMemento details,
        @JsonProperty("state") VersionState state,
        @JsonProperty("updated") Date updated) {

        return new VersionStatus(details, state, updated);
    }

    public static VersionStatus apply(VersionDetails details, VersionState state, Date updated) {
        return apply(details.memento(), state, updated);
    }

}
