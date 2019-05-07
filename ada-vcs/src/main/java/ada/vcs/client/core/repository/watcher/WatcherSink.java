package ada.vcs.client.core.repository.watcher;

import ada.vcs.client.core.repository.api.RepositorySink;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.server.domain.repository.Protocol;
import akka.actor.typed.ActorRef;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class WatcherSink implements RepositorySink {

    private final RepositorySink actual;

    private final ActorRef<Protocol.RepositoryMessage> actor;

    @Override
    public Sink<ByteString, CompletionStage<VersionDetails>> get() {
        return actual.get();
    }

}
