package ada.cli.commands.repository.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.ada.model.HttpEndpoint;
import com.ibm.ada.model.ResourceName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Optional;

@Value
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Remote {

    /**
     * The user provided name of the remote.
     */
    private final ResourceName name;

    /**
     * The Http endpoint which provides the repository API (including the resource name).
     */
    private final HttpEndpoint endpoint;

    /**
     * Status information fetched from remote repository.
     */
    private final Fetched fetched;

    @JsonCreator
    public static Remote apply(
        @JsonProperty("name") ResourceName name, @JsonProperty("endpoint") HttpEndpoint endpoint,
        @JsonProperty("fetched") Fetched fetched) {

        return new Remote(name, endpoint, fetched);
    }

    public static Remote apply(ResourceName name, HttpEndpoint endpoint) {
        return apply(name, endpoint, null);
    }

    public Optional<Fetched> getFetched() {
        return Optional.ofNullable(fetched);
    }

}
