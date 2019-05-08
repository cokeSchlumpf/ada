package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.shared.converters.api.DataSinkMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class TargetMemento {

    @JsonProperty("alias")
    private final ResourceName alias;

    @JsonProperty("sink")
    private final DataSinkMemento sink;

    @JsonCreator
    public static TargetMemento apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("sink") DataSinkMemento sink) {

        return new TargetMemento(alias, sink);
    }

}
