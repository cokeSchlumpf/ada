package ada.vcs.shared.repository.watcher;

import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.shared.repository.api.RepositorySourceMemento;
import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WatcherSourceMemento implements RepositorySourceMemento {

    private static final String ACTUAL = "actual";

    private static final String REPOSITORY_ACTOR = "repository-actor";

    @JsonProperty(ACTUAL)
    private final RepositorySourceMemento actual;

    @JsonProperty(REPOSITORY_ACTOR)
    private final ActorRef<RepositoryMessage> repositoryActor;

    public static WatcherSourceMemento apply(
        @JsonProperty(ACTUAL) RepositorySourceMemento actual,
        @JsonProperty(REPOSITORY_ACTOR) ActorRef<RepositoryMessage> repositoryActor) {

        return new WatcherSourceMemento(actual, repositoryActor);
    }

}
