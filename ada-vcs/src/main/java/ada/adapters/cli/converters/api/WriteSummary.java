package ada.adapters.cli.converters.api;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class WriteSummary {

    private long count;

}
