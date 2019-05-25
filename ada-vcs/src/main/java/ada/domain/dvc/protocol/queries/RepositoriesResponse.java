package ada.domain.dvc.protocol.queries;

import ada.domain.dvc.values.RepositorySummary;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositoriesResponse {

    private final ImmutableList<RepositorySummary> repositories;

    @JsonCreator
    public static RepositoriesResponse apply(
        @JsonProperty("repositories") List<RepositorySummary> repositories) {

        return apply(ImmutableList.copyOf(repositories));
    }

    public static RepositoriesResponse apply() {
        return apply(ImmutableList.of());
    }

}
