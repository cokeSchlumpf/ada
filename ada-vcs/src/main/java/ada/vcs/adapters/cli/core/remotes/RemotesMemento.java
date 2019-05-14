package ada.vcs.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RemotesMemento {

    private final List<RemoteMemento> remotes;

    private final ResourceName upstream;

    @JsonCreator
    public static RemotesMemento apply(
        @JsonProperty("remotes") List<RemoteMemento> remotes,
        @JsonProperty("upstream") ResourceName upstream) {

        return new RemotesMemento(Lists.newArrayList(remotes), upstream);
    }

}
