package ada.vcs.domain.dvc.services.repositories;

import ada.commons.util.FQResourceName;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesResponse;
import ada.vcs.domain.dvc.protocol.values.User;
import akka.actor.typed.ActorRef;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@AllArgsConstructor(staticName = "apply")
public class StartRepositoriesQuery$New {

    private final User executor;

    private final ActorRef<RepositoriesResponse> replyTo;

    private final ImmutableSet<FQResourceName> repositories;

    public static StartRepositoriesQuery$New apply(
        User executor,
        ActorRef<RepositoriesResponse> replyTo,
        Set<FQResourceName> repositories) {

        return apply(executor, replyTo, ImmutableSet.copyOf(repositories));
    }

}
