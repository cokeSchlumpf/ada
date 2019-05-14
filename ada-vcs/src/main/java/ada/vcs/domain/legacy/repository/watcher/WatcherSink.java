package ada.vcs.domain.legacy.repository.watcher;

import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.legacy.repository.api.RepositorySink;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import akka.actor.typed.ActorRef;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class WatcherSink implements RepositorySink {

    private final RepositorySink actual;

    private final ActorRef<RepositoryMessage> actor;

    @Override
    public Sink<ByteString, CompletionStage<VersionDetails>> get() {
        return actual.get();
    }

}
