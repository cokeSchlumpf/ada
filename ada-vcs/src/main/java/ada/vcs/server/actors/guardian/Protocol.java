package ada.vcs.server.actors.guardian;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.file.Path;

import static ada.vcs.server.actors.repository.Protocol.*;

public abstract class Protocol {

    private Protocol() {

    }

    public interface GuardianMessage {

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static final class StartRepository implements GuardianMessage {

        private final ActorRef<RepositoryStarted> replyTo;

        private final Path root;

    }

    @AllArgsConstructor(staticName = "apply")
    public static final class RepositoryStarted {

        private final ActorRef<RepositoryManagerMessage> repository;

    }

}
