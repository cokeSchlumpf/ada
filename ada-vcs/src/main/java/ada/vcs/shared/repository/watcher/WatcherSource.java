package ada.vcs.shared.repository.watcher;

import ada.vcs.shared.repository.api.RepositorySource;
import ada.vcs.shared.repository.api.version.VersionDetails;
import ada.vcs.server.domain.repository.entities.Protocol;
import akka.actor.typed.ActorRef;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class WatcherSource implements RepositorySource {

    private final RepositorySource actual;

    private final ActorRef<Protocol.RepositoryMessage> actor;

    @Override
    public Source<ByteString, CompletionStage<VersionDetails>> get() {
        return actual.get();
    }

}
