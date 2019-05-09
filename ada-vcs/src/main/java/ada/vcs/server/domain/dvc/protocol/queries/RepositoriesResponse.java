package ada.vcs.server.domain.dvc.protocol.queries;

import ada.vcs.server.domain.dvc.values.RepositorySummary;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositoriesResponse {

    private final ImmutableList<RepositorySummary> repositories;

    public static RepositoriesResponse apply(List<RepositorySummary> repositories) {
        return apply(ImmutableList.copyOf(repositories));
    }

}
