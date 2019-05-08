package ada.vcs.server.domain.repository;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.shared.repository.api.RefSpec;
import ada.vcs.shared.repository.api.version.VersionDetailsMemento;
import ada.vcs.shared.repository.api.RepositorySinkMemento;
import ada.vcs.shared.repository.api.RepositorySourceMemento;
import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

public abstract class Protocol {

    private Protocol() {

    }

    public interface DataVersionControlMessage {

    }

    public interface NamespaceMessage extends DataVersionControlMessage {

        ResourceName getNamespace();

    }

    public interface RepositoryMessage extends NamespaceMessage {

        ResourceName getRepository();

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static final class CreateRepository implements NamespaceMessage {

        private final String id;

        private final ResourceName namespace;

        private final ResourceName repository;

        private final ActorRef<RepositoryCreated> replyTo;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static final class RepositoryCreated {

        private final String id;

        private final ResourceName namespace;

        private final ResourceName repository;

        private final ActorRef<RepositoryMessage> repositoryActor;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static final class Push implements RepositoryMessage {

        private final String id;

        private final ResourceName namespace;

        private final ResourceName repository;

        private final VersionDetailsMemento details;

        private final ActorRef<RepositorySinkMemento> replyTo;

        private final ActorRef<RefSpecAlreadyExistsError> handleError;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static final class Pull implements RepositoryMessage {

        private final String id;

        private final ResourceName namespace;

        private final ResourceName repository;

        private final RefSpec refSpec;

        private final ActorRef<RepositorySourceMemento> replyTo;

        private final ActorRef<RefSpecNotFoundError> handleError;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static final class RefSpecNotFoundError implements ErrorMessage {

        private final String id;

        private final ResourceName namespace;

        private final ResourceName repository;

        private final RefSpec refSpec;

        @Override
        @JsonIgnore
        public String getMessage() {
            return String.format(
                "The reference %s was not found in repository %s/%s",
                refSpec, namespace.getValue(), repository.getValue());
        }

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static final class RefSpecAlreadyExistsError implements ErrorMessage {

        private final String id;

        private final ResourceName namespace;

        private final ResourceName repository;

        private final RefSpec refSpec;

        @Override
        @JsonIgnore
        public String getMessage() {
            return String.format(
                "The reference '%s' already exists in %s/%s",
                refSpec, namespace.getValue(), repository.getValue());
        }

    }

}
