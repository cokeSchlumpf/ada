package ada.vcs.client.core.dataset;

import ada.vcs.client.core.remotes.RemoteMemento;
import ada.vcs.shared.repository.api.version.VersionDetailsMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteSourceMemento {

    private final VersionDetailsMemento details;

    private final RemoteMemento remote;

    @JsonCreator
    public static RemoteSourceMemento apply(
        @JsonProperty("details") VersionDetailsMemento id,
        @JsonProperty("remote") RemoteMemento remote) {

        return new RemoteSourceMemento(id, remote);
    }

}
