package ada.vcs.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NamespaceRemoved implements DataVersionControlEvent {

    private final ResourceName namespace;

    @JsonCreator
    public static NamespaceRemoved apply(
        @JsonProperty("namespace") ResourceName namespace) {

        return new NamespaceRemoved(namespace);
    }

}
