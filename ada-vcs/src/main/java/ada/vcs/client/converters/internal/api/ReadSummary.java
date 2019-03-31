package ada.vcs.client.converters.internal.api;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class ReadSummary<T extends Context> {

    private T context;

    private long success;

    private long failure;

}
