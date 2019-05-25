package ada.domain.dvc.protocol.queries;

import ada.commons.util.ResourceName;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositoriesInNamespaceResponse {

    private final ResourceName namespace;

    private final ImmutableSet<ResourceName> repositories;

    public static RepositoriesInNamespaceResponse apply(
        ResourceName namespace,
        Set<ResourceName> repositories) {

        return apply(namespace, ImmutableSet.copyOf(repositories));
    }

}
