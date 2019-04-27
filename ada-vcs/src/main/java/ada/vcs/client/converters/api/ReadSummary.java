package ada.vcs.client.converters.api;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class ReadSummary {

    private long success;

    private long failure;

}
