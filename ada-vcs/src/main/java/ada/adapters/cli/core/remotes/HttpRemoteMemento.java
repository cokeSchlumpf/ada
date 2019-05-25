package ada.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.net.URL;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class HttpRemoteMemento implements RemoteMemento {

    private final ResourceName alias;

    private final URL endpoint;

    @JsonCreator
    public static HttpRemoteMemento apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("endpoint") URL endpoint) {

        return new HttpRemoteMemento(alias, endpoint);
    }

}
