package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.net.URL;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRemote implements Remote {

    private final ResourceName alias;

    private final URL endpoint;

    @JsonCreator
    public static HttpRemote apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("endpoint") URL endpoint) {

        return new HttpRemote(alias, endpoint);
    }

    @Override
    public String getInfo() {
        return endpoint.toString();
    }
}
