package ada.vcs.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.values.User;
import ada.vcs.domain.dvc.values.VersionStatus;
import ada.vcs.domain.legacy.repository.api.RefSpec;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class SubmitPushInRepository implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final RefSpec.VersionRef refSpec;

    private final ActorRef<VersionStatus> replyTo;

    private final ActorRef<ErrorMessage> errorTo;

}
