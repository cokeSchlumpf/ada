package ada.vcs.client.converters.internal.monitors;

import ada.vcs.client.converters.api.Monitor;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class NoOpMonitor implements Monitor {

    @Override
    public void processed() {

    }

    @Override
    public void warning(long record, String field, String message) {

    }

}
