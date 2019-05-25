package ada.domain.legacy.repository.watcher;

import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.legacy.repository.api.version.VersionDetails;
import ada.domain.legacy.repository.api.RepositorySource;
import akka.actor.typed.ActorRef;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class WatcherSource implements RepositorySource {

    private final RepositorySource actual;

    private final ActorRef<RepositoryMessage> actor;

    @Override
    public Source<ByteString, CompletionStage<VersionDetails>> get() {
        return actual.get();
    }

}
