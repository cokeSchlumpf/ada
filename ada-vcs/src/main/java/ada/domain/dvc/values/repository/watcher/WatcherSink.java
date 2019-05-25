package ada.domain.dvc.values.repository.watcher;

import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.values.repository.RepositorySink;
import ada.domain.dvc.values.repository.version.VersionDetails;
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
