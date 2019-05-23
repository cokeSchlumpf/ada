package ada.vcs.domain.dvc.entities.repository;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.protocol.api.RepositoryEvent;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.commands.*;
import ada.vcs.domain.dvc.protocol.errors.RefSpecAlreadyExistsError;
import ada.vcs.domain.dvc.protocol.errors.RefSpecNotFoundError;
import ada.vcs.domain.dvc.protocol.events.*;
import ada.vcs.domain.dvc.protocol.queries.*;
import ada.vcs.domain.dvc.services.RemoveRepositoryDataSaga;
import ada.vcs.domain.dvc.values.*;
import ada.vcs.domain.legacy.repository.api.RefSpec;
import ada.vcs.domain.legacy.repository.api.RepositorySinkMemento;
import ada.vcs.domain.legacy.repository.api.RepositorySourceMemento;
import ada.vcs.domain.legacy.repository.api.RepositoryStorageAdapter;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import ada.vcs.domain.legacy.repository.watcher.WatcherSinkMemento;
import ada.vcs.domain.legacy.repository.watcher.WatcherSourceMemento;
import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EffectFactories;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply")
public final class RepositoryActive implements State {

    private final ActorContext<RepositoryMessage> actor;

    private final EffectFactories<RepositoryEvent, State> effect;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    private final Date createdAt;

    private final UserId createdBy;

    private Map<RefSpec.VersionRef, VersionStatus> versions;

    private Map<RefSpec.TagRef, RefSpec.VersionRef> tags;

    private RepositoryAuthorizations authorizations;

    public static RepositoryActive apply(
        ActorContext<RepositoryMessage> actor, EffectFactories<RepositoryEvent, State> effect, CommandContext context,
        RepositoryStorageAdapter repositoryStorageAdapter, ResourceName namespace, ResourceName name,
        Date createdAt, UserId createdBy) {

        return apply(
            actor, effect, context, repositoryStorageAdapter, namespace, name,
            createdAt, createdBy, Maps.newHashMap(), Maps.newHashMap(),
            RepositoryAuthorizations.apply(namespace, name));
    }

    @Override
    public Effect<RepositoryEvent, State> onCreate(CreateRepository create) {
        RepositoryCreated created = RepositoryCreated
            .apply(create.getId(), namespace, name, createdBy, createdAt);

        create.getReplyTo().tell(created);
        return effect.none();
    }

    @Override
    public State onCreated(RepositoryCreated created) {
        return this;
    }

    @Override
    public Effect<RepositoryEvent, State> onDetailsRequest(RepositoryDetailsRequest request) {
        List<VersionStatus> versionStatuses = Ordering
            .natural()
            .reverse()
            .onResultOf(VersionStatus::getUpdated)
            .sortedCopy(versions.values());

        RepositoryDetailsResponse response = RepositoryDetailsResponse.apply(
            request.getId(),
            request.getNamespace(),
            request.getRepository(),
            getSummary(),
            authorizations,
            versionStatuses);

        request.getReplyTo().tell(response);

        return effect.none();
    }

    @Override
    public Effect<RepositoryEvent, State> onGrantAccess(GrantAccessToRepository grant) {
        return authorizations
            .getAuthorizations()
            .stream()
            .filter(g -> g.getAuthorization().equals(grant.getAuthorization()))
            .findFirst()
            .map(granted -> {
                grant
                    .getReplyTo()
                    .tell(GrantedAccessToRepository.apply(
                        grant.getId(), grant.getNamespace(),
                        grant.getRepository(), granted));

                return effect.none();
            })
            .orElseGet(() -> {
                GrantedAuthorization granted = GrantedAuthorization.apply(grant.getExecutor(), new Date(), grant.getAuthorization());
                GrantedAccessToRepository event = GrantedAccessToRepository.apply(
                    grant.getId(), grant.getNamespace(),
                    grant.getRepository(), granted);

                return effect
                    .persist(event)
                    .thenRun(() -> grant.getReplyTo().tell(event));
            });
    }

    @Override
    public State onGrantedAccess(GrantedAccessToRepository granted) {
        authorizations = authorizations.add(granted.getAuthorization());
        return this;
    }

    @Override
    public Effect<RepositoryEvent, State> onPull(Pull pull) {
        RefSpec.VersionRef versionRef = refSpecToVersionRef(pull.getRefSpec());

        if (versionRef != null) {
            RepositorySourceMemento actualSourceMemento = repositoryStorageAdapter.pull(namespace, name, versionRef);
            WatcherSourceMemento watcherSourceMemento = WatcherSourceMemento.apply(actualSourceMemento, actor.getSelf());

            pull.getReplyTo().tell(watcherSourceMemento);
        } else {
            RefSpecNotFoundError response = RefSpecNotFoundError.apply(
                pull.getId(),
                pull.getNamespace(),
                pull.getRepository(),
                pull.getRefSpec());

            pull.getErrorTo().tell(response);
        }

        return effect.none();
    }

    @Override
    public Effect<RepositoryEvent, State> onPush(Push push) {
        RefSpec.VersionRef versionRef = RefSpec.VersionRef.apply(push.getDetails().getId());

        if (!versions.containsKey(versionRef)) {
            VersionDetails details = context.factories().versionFactory().createDetails(push.getDetails());
            RepositorySinkMemento actualSinkMemento = repositoryStorageAdapter.push(namespace, name, details);

            WatcherSinkMemento watcherSinkMemento = WatcherSinkMemento.apply(actualSinkMemento, actor.getSelf());

            VersionStatus status = VersionStatus.apply(details, VersionState.INITIALIZED, new Date());
            VersionUpsertedInRepository event = VersionUpsertedInRepository.apply(push.getNamespace(), push.getRepository(), status);

            return effect
                .persist(event)
                .thenRun(() -> push.getReplyTo().tell(watcherSinkMemento));
        } else {
            RefSpecAlreadyExistsError error = RefSpecAlreadyExistsError.apply(
                push.getId(),
                push.getNamespace(),
                push.getRepository(),
                versionRef);

            push.getErrorTo().tell(error);

            return effect.none();
        }
    }

    @Override
    public Effect<RepositoryEvent, State> onRemove(RemoveRepository remove) {
        RepositoryRemoved removed = RepositoryRemoved.apply(namespace, name);
        return effect
            .persist(removed)
            .thenRun(() -> {
                remove.getReplyTo().tell(removed);
                actor.spawn(
                    RemoveRepositoryDataSaga.createBehavior(namespace, name, repositoryStorageAdapter, versions.keySet()),
                    String.format("remove-%s", Operators.hash()));
            });
    }

    @Override
    public State onRemoved(RepositoryRemoved removed) {
        return RepositoryUninitialized.apply(actor, effect, context, repositoryStorageAdapter, namespace, name);
    }

    @Override
    public Effect<RepositoryEvent, State> onRevokeAccess(RevokeAccessFromRepository revoke) {
        return authorizations
            .getAuthorizations()
            .stream()
            .filter(g -> g.getAuthorization().equals(revoke.getAuthorization()))
            .findFirst()
            .map(granted -> {
                RevokedAccessFromRepository event = RevokedAccessFromRepository.apply(
                    revoke.getId(), revoke.getNamespace(), revoke.getRepository(), granted);

                return effect
                    .persist(event)
                    .thenRun(() -> revoke.getReplyTo().tell(event));
            })
            .orElseGet(() -> {
                GrantedAuthorization granted = GrantedAuthorization.apply(
                    revoke.getExecutor(), new Date(), revoke.getAuthorization());

                RevokedAccessFromRepository event = RevokedAccessFromRepository.apply(
                    revoke.getId(), revoke.getNamespace(), revoke.getRepository(), granted);

                revoke.getReplyTo().tell(event);

                return effect.none();
            });
    }

    @Override
    public State onRevokedAccess(RevokedAccessFromRepository revoked) {
        authorizations = authorizations.remove(revoked.getAuthorization().getAuthorization());
        return this;
    }

    @Override
    public Effect<RepositoryEvent, State> onSubmit(SubmitPushInRepository submit) {
        final VersionStatus currentStatus = versions.get(submit.getRefSpec());

        if (currentStatus == null) {
            RefSpecNotFoundError error = RefSpecNotFoundError.apply(
                submit.getId(), submit.getNamespace(), submit.getRepository(),
                submit.getRefSpec());

            submit.getErrorTo().tell(error);

            return effect.none();
        } else {
            final VersionStatus newStatus = currentStatus
                .withState(VersionState.PUSHED)
                .withUpdated(new Date());

            VersionUpsertedInRepository event = VersionUpsertedInRepository.apply(namespace, name, newStatus);

            return effect
                .persist(event)
                .thenRun(() -> submit.getReplyTo().tell(newStatus));
        }
    }

    @Override
    public Effect<RepositoryEvent, State> onSummaryRequest(RepositorySummaryRequest request) {
        boolean isAuthorized = authorizations.isAuthorized(request);

        if (isAuthorized) {
            RepositorySummary summary = getSummary();

            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name, summary);
            request.getReplyTo().tell(response);
        } else {
            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name);
            request.getReplyTo().tell(response);
        }

        return effect.none();
    }

    @Override
    public State onUpserted(VersionUpsertedInRepository upserted) {
        RefSpec.VersionRef versionRef = RefSpec.fromId(upserted.getStatus().getDetails().getId());
        versions.put(versionRef, upserted.getStatus());
        return this;
    }

    @Override
    public boolean isAuthorized(RepositoryMessage message) {
        return authorizations.isAuthorized(message);
    }

    private RepositorySummary getSummary() {
        if (versions.isEmpty()) {
            return RepositorySummary.apply(namespace, name, createdAt);
        } else {
            List<VersionDetails> detailsList = versions
                .values()
                .stream()
                .map(status -> context.factories().versionFactory().createDetails(status.getDetails()))
                .collect(Collectors.toList());

            VersionDetails details = Ordering
                .natural()
                .reverse()
                .onResultOf(VersionDetails::date)
                .sortedCopy(detailsList)
                .get(0);

            return RepositorySummary.apply(namespace, name, details.date(), details.id());
        }
    }

    private RefSpec.VersionRef refSpecToVersionRef(RefSpec refSpec) {
        if (refSpec instanceof RefSpec.VersionRef && versions.containsKey(refSpec)) {
            return (RefSpec.VersionRef) refSpec;
        } else if (refSpec instanceof RefSpec.TagRef) {
            return tags.get(refSpec);
        } else {
            return null;
        }
    }

}
