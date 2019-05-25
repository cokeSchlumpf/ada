package ada.domain.dvc.entities.repository;

import ada.adapters.cli.commands.context.CommandContext;
import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.RepositoryEvent;
import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.protocol.commands.*;
import ada.domain.dvc.protocol.events.*;
import ada.domain.dvc.protocol.queries.Pull;
import ada.domain.dvc.protocol.queries.RepositoryDetailsRequest;
import ada.domain.dvc.protocol.queries.RepositorySummaryRequest;
import ada.domain.dvc.protocol.queries.RepositorySummaryResponse;
import ada.domain.dvc.values.RepositoryAuthorizations;
import ada.domain.legacy.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EffectFactories;
import lombok.AllArgsConstructor;

import java.util.Date;

@AllArgsConstructor(staticName = "apply")
public final class RepositoryUninitialized implements State {

    private final ActorContext<RepositoryMessage> actor;

    private final EffectFactories<RepositoryEvent, State> effect;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    @Override
    public Effect<RepositoryEvent, State> onCreate(CreateRepository create) {
        RepositoryCreated created = RepositoryCreated.apply(
            create.getId(), namespace, name, create.getExecutor().getUserId(), new Date());

        return effect
            .persist(created)
            .thenRun(() -> create.getReplyTo().tell(created));
    }

    @Override
    public Effect<RepositoryEvent, State> onDetailsRequest(RepositoryDetailsRequest request) {
        return null;
    }

    @Override
    public State onCreated(RepositoryCreated created) {
        return RepositoryActive.apply(
            actor, effect, context, repositoryStorageAdapter,
            namespace, name, created.getCreated(), created.getUserId());
    }

    @Override
    public Effect<RepositoryEvent, State> onGrantAccess(GrantAccessToRepository grant) {
        return effect.none();
    }

    @Override
    public State onGrantedAccess(GrantedAccessToRepository granted) {
        return this;
    }

    @Override
    public Effect<RepositoryEvent, State> onPull(Pull pull) {
        return effect.none();
    }

    @Override
    public Effect<RepositoryEvent, State> onPush(Push push) {
        return effect.none();
    }

    @Override
    public Effect<RepositoryEvent, State> onRemove(RemoveRepository remove) {
        return effect.none();
    }

    @Override
    public State onRemoved(RepositoryRemoved removed) {
        return this;
    }

    @Override
    public Effect<RepositoryEvent, State> onRevokeAccess(RevokeAccessFromRepository revoke) {
        return effect.none();
    }

    @Override
    public State onRevokedAccess(RevokedAccessFromRepository revoked) {
        return this;
    }

    @Override
    public Effect<RepositoryEvent, State> onSubmit(SubmitPushInRepository submit) {
        return effect.none();
    }

    @Override
    public Effect<RepositoryEvent, State> onSummaryRequest(RepositorySummaryRequest request) {
        RepositorySummaryResponse response = RepositorySummaryResponse
            .apply(request.getId(), namespace, name);

        request.getReplyTo().tell(response);

        return effect.none();
    }

    @Override
    public State onUpserted(VersionUpsertedInRepository upserted) {
        return this;
    }

    @Override
    public boolean isAuthorized(RepositoryMessage message) {
        return RepositoryAuthorizations.apply(namespace, name).isAuthorized(message);
    }

}
