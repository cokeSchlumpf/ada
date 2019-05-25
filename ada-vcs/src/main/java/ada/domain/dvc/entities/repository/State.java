package ada.domain.dvc.entities.repository;

import ada.domain.dvc.protocol.api.RepositoryEvent;
import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.protocol.commands.*;
import ada.domain.dvc.protocol.events.*;
import ada.domain.dvc.protocol.queries.Pull;
import ada.domain.dvc.protocol.queries.RepositoryDetailsRequest;
import ada.domain.dvc.protocol.queries.RepositorySummaryRequest;
import akka.persistence.typed.javadsl.Effect;

public interface State {

    Effect<RepositoryEvent, State> onCreate(CreateRepository create);

    Effect<RepositoryEvent, State> onDetailsRequest(RepositoryDetailsRequest request);

    State onCreated(RepositoryCreated created);

    Effect<RepositoryEvent, State> onGrantAccess(GrantAccessToRepository grant);

    State onGrantedAccess(GrantedAccessToRepository granted);

    Effect<RepositoryEvent, State> onPull(Pull pull);

    Effect<RepositoryEvent, State> onPush(Push push);

    Effect<RepositoryEvent, State> onRemove(RemoveRepository remove);

    State onRemoved(RepositoryRemoved removed);

    Effect<RepositoryEvent, State> onRevokeAccess(RevokeAccessFromRepository revoke);

    State onRevokedAccess(RevokedAccessFromRepository revoked);

    Effect<RepositoryEvent, State> onSubmit(SubmitPushInRepository submit);

    Effect<RepositoryEvent, State> onSummaryRequest(RepositorySummaryRequest request);

    State onUpserted(VersionUpsertedInRepository upserted);

    boolean isAuthorized(RepositoryMessage message);

}
