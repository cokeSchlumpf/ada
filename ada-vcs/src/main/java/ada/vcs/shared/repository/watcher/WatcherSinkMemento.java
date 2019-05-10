package ada.vcs.shared.repository.watcher;

import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.shared.repository.api.RepositorySinkMemento;
import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WatcherSinkMemento implements RepositorySinkMemento {

    private static final String ACTUAL = "actual";
    private static final String REPOSITORY_ACTOR = "repository-actor";

    @JsonProperty(ACTUAL)
    private final RepositorySinkMemento actual;

    @JsonProperty(REPOSITORY_ACTOR)
    private final ActorRef<RepositoryMessage> repositoryActor;

    @JsonCreator
    public static WatcherSinkMemento apply(
        @JsonProperty(ACTUAL) RepositorySinkMemento actual,
        @JsonProperty(REPOSITORY_ACTOR) ActorRef<RepositoryMessage> repositoryActor) {

        return new WatcherSinkMemento(actual, repositoryActor);
    }

}