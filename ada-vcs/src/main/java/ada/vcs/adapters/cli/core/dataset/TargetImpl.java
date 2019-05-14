package ada.vcs.adapters.cli.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.domain.legacy.converters.api.DataSink;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
final class TargetImpl implements Target {

    private final ResourceName alias;

    private final DataSink sink;

    @Override
    public ResourceName alias() {
        return alias;
    }

    @Override
    public DataSink sink() {
        return sink;
    }

    @Override
    public Target withAlias(ResourceName alias) {
        return TargetImpl.apply(alias, sink);
    }

    @Override
    public int compareTo(Target o) {
        return 0;
    }
}
