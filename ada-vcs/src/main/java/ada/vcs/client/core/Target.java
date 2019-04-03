package ada.vcs.client.core;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.internal.api.DataSink;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class Target {

    private final ResourceName alias;

    private final DataSink sink;

}
