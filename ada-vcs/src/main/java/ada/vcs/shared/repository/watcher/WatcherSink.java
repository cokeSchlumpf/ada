package ada.vcs.shared.repository.watcher;

import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.shared.repository.api.RepositorySink;
import ada.vcs.shared.repository.api.version.VersionDetails;
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
