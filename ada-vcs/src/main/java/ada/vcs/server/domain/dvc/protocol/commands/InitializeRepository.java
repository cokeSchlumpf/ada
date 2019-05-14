package ada.vcs.server.domain.dvc.protocol.commands;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.values.User;
import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(staticName = "apply")
public class InitializeRepository implements RepositoryMessage {

    private final String id;

    private final User executor;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final ActorRef<ErrorMessage> errorTo;

    private final Date date;

}
