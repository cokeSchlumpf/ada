package ada.vcs.domain.dvc.entities;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.protocol.api.RepositoryEvent;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.commands.*;
import ada.vcs.domain.dvc.protocol.errors.RefSpecAlreadyExistsError;
import ada.vcs.domain.dvc.protocol.errors.RefSpecNotFoundError;
import ada.vcs.domain.dvc.protocol.errors.UserNotAuthorizedError;
import ada.vcs.domain.dvc.protocol.events.GrantedAccessToRepository;
import ada.vcs.domain.dvc.protocol.events.RepositoryInitialized;
import ada.vcs.domain.dvc.protocol.events.RevokedAccessToRepository;
import ada.vcs.domain.dvc.protocol.events.VersionUpsertedInRepository;
import ada.vcs.domain.dvc.protocol.queries.*;
import ada.vcs.domain.dvc.values.*;
import ada.vcs.domain.legacy.repository.api.RefSpec;
import ada.vcs.domain.legacy.repository.api.RepositorySinkMemento;
import ada.vcs.domain.legacy.repository.api.RepositorySourceMemento;
import ada.vcs.domain.legacy.repository.api.RepositoryStorageAdapter;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import ada.vcs.domain.legacy.repository.watcher.WatcherSinkMemento;
import ada.vcs.domain.legacy.repository.watcher.WatcherSourceMemento;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/*
 * TODO mw:
 *  - Implement request for repository details
 */
public final class Repository extends EventSourcedBehavior<RepositoryMessage, RepositoryEvent, Repository.State> {

    private final ActorContext<RepositoryMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    public Repository(
        PersistenceId persistenceId, ActorContext<RepositoryMessage> actor, CommandContext context,
        RepositoryStorageAdapter repositoryStorageAdapter, ResourceName namespace, ResourceName name) {

        super(persistenceId);
        this.actor = actor;
        this.context = context;
        this.repositoryStorageAdapter = repositoryStorageAdapter;
        this.namespace = namespace;
        this.name = name;
    }

    public static Behavior<RepositoryMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter,
        ResourceName namespace, ResourceName name) {

        String persistenceId = String.format("dvc/%s/%s", namespace.getValue(), name.getValue());

        return Behaviors.setup(actor -> new Repository(
            PersistenceId.apply(persistenceId), actor, context, repositoryStorageAdapter, namespace, name));
    }

    @Override
    public State emptyState() {
        return State.apply(Maps.newHashMap(), Maps.newHashMap(), RepositoryAuthorizations.apply(namespace, name), null);
    }

    @Override
    public CommandHandler<RepositoryMessage, RepositoryEvent, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(GrantAccessToRepository.class, whenChecked(this::onGrant))
            .onCommand(InitializeRepository.class, whenChecked(this::onInitialize))
            .onCommand(Pull.class, whenChecked(this::onPull))
            .onCommand(Push.class, whenChecked(this::onPush))
            .onCommand(RepositoryDetailsRequest.class, whenChecked(this::onRepositoryDetailsRequest))
            .onCommand(RepositorySummaryRequest.class, whenResponsible(this::onSummaryRequest))
            .onCommand(RevokeAccessToRepository.class, whenChecked(this::onRevoke))
            .onCommand(SubmitPushInRepository.class, whenChecked(this::onSubmit))
            .build();
    }

    @Override
    public EventHandler<State, RepositoryEvent> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(GrantedAccessToRepository.class, this::onGranted)
            .onEvent(RevokedAccessToRepository.class, this::onRevoked)
            .onEvent(RepositoryInitialized.class, this::onInitialized)
            .onEvent(VersionUpsertedInRepository.class, this::onUpsert)
            .build();
    }

    /*
     * Message handlers
     */

    private Effect<RepositoryEvent, State> onGrant(State state, GrantAccessToRepository grant) {
        return state
            .authorizations
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

                return Effect().none();
            })
            .orElseGet(() -> {
                GrantedAuthorization granted = GrantedAuthorization.apply(grant.getExecutor(), new Date(), grant.getAuthorization());
                GrantedAccessToRepository event = GrantedAccessToRepository.apply(
                    grant.getId(), grant.getNamespace(),
                    grant.getRepository(), granted);

                return Effect()
                    .persist(event)
                    .thenRun(() -> grant.getReplyTo().tell(event));
            });
    }

    private State onGranted(State state, GrantedAccessToRepository granted) {
        state.authorizations = state.authorizations.add(granted.getAuthorization());
        return state;
    }

    @SuppressWarnings("unused")
    private Effect<RepositoryEvent, State> onInitialize(State state, InitializeRepository init) {
        RepositoryInitialized initialized = RepositoryInitialized.apply(init.getDate(), init.getExecutor().getUserId());
        return Effect().persist(initialized);
    }

    private State onInitialized(State state, RepositoryInitialized init) {
        state.created = init.getDate();
        return state;
    }

    private Effect<RepositoryEvent, State> onPull(State state, Pull pull) {
        RefSpec.VersionRef versionRef = refSpecToVersionRef(state, pull.getRefSpec());

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

        return Effect().none();
    }

    private Effect<RepositoryEvent, State> onPush(State state, Push push) {
        RefSpec.VersionRef versionRef = RefSpec.VersionRef.apply(push.getDetails().getId());

        if (!state.versions.containsKey(versionRef)) {
            VersionDetails details = context.factories().versionFactory().createDetails(push.getDetails());
            RepositorySinkMemento actualSinkMemento = repositoryStorageAdapter.push(namespace, name, details);

            WatcherSinkMemento watcherSinkMemento = WatcherSinkMemento.apply(actualSinkMemento, actor.getSelf());

            VersionStatus status = VersionStatus.apply(details, VersionState.INITIALIZED, new Date());
            VersionUpsertedInRepository event = VersionUpsertedInRepository.apply(push.getNamespace(), push.getRepository(), status);

            return Effect()
                .persist(event)
                .thenRun(() -> push.getReplyTo().tell(watcherSinkMemento));
        } else {
            RefSpecAlreadyExistsError error = RefSpecAlreadyExistsError.apply(
                push.getId(),
                push.getNamespace(),
                push.getRepository(),
                versionRef);

            push.getErrorTo().tell(error);

            return Effect().none();
        }
    }

    private Effect<RepositoryEvent, State> onRepositoryDetailsRequest(State state, RepositoryDetailsRequest request) {
        List<VersionStatus> versionStatuses = Ordering
            .natural()
            .reverse()
            .onResultOf(VersionStatus::getUpdated)
            .sortedCopy(state.versions.values());

        RepositoryDetailsResponse response = RepositoryDetailsResponse.apply(
            request.getId(),
            request.getNamespace(),
            request.getRepository(),
            getSummary(state),
            state.authorizations,
            versionStatuses);

        request.getReplyTo().tell(response);

        return Effect().none();
    }

    private Effect<RepositoryEvent, State> onSubmit(State state, SubmitPushInRepository submit) {
        final VersionStatus currentStatus = state.versions.get(submit.getRefSpec());

        if (currentStatus == null) {
            RefSpecNotFoundError error = RefSpecNotFoundError.apply(
                submit.getId(), submit.getNamespace(), submit.getRepository(),
                submit.getRefSpec());

            submit.getErrorTo().tell(error);

            return Effect().none();
        } else {
            final VersionStatus newStatus = currentStatus
                .withState(VersionState.PUSHED)
                .withUpdated(new Date());

            VersionUpsertedInRepository event = VersionUpsertedInRepository.apply(namespace, name, newStatus);

            return Effect()
                .persist(event)
                .thenRun(() -> submit.getReplyTo().tell(newStatus));
        }
    }

    private Effect<RepositoryEvent, State> onRevoke(State state, RevokeAccessToRepository revoke) {
        return state
            .authorizations
            .getAuthorizations()
            .stream()
            .filter(g -> g.getAuthorization().equals(revoke.getAuthorization()))
            .findFirst()
            .map(granted -> {
                RevokedAccessToRepository event = RevokedAccessToRepository.apply(
                    revoke.getId(), revoke.getNamespace(), revoke.getRepository(), granted);

                return Effect()
                    .persist(event)
                    .thenRun(() -> revoke.getReplyTo().tell(event));
            })
            .orElseGet(() -> {
                GrantedAuthorization granted = GrantedAuthorization.apply(
                    revoke.getExecutor(), new Date(), revoke.getAuthorization());

                RevokedAccessToRepository event = RevokedAccessToRepository.apply(
                    revoke.getId(), revoke.getNamespace(), revoke.getRepository(), granted);

                revoke.getReplyTo().tell(event);

                return Effect().none();
            });
    }

    private State onRevoked(State state, RevokedAccessToRepository revoked) {
        state.authorizations = state.authorizations.remove(revoked.getAuthorization().getAuthorization());
        return state;
    }

    private Effect<RepositoryEvent, State> onSummaryRequest(State state, RepositorySummaryRequest request) {
        boolean isAuthorized = state.authorizations.isAuthorized(request).orElse(false);
        if (isAuthorized) {
            RepositorySummary summary = getSummary(state);

            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name, summary);
            request.getReplyTo().tell(response);
        } else {
            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name);
            request.getReplyTo().tell(response);
        }

        return Effect().none();
    }

    private State onUpsert(State state, VersionUpsertedInRepository upserted) {
        RefSpec.VersionRef versionRef = RefSpec.fromId(upserted.getStatus().getDetails().getId());
        state.versions.put(versionRef, upserted.getStatus());
        return state;
    }

    /*
     * Helper functions
     */

    private RepositorySummary getSummary(State state) {
        if (state.versions.isEmpty()) {
            return RepositorySummary.apply(namespace, name, state.created, null);
        } else {
            List<VersionDetails> detailsList = state
                .versions
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

    private RefSpec.VersionRef refSpecToVersionRef(State state, RefSpec refSpec) {
        if (refSpec instanceof RefSpec.VersionRef && state.versions.containsKey(refSpec)) {
            return (RefSpec.VersionRef) refSpec;
        } else if (refSpec instanceof RefSpec.TagRef) {
            return state.tags.get(refSpec);
        } else {
            return null;
        }
    }

    private <T extends RepositoryMessage> BiFunction<State, T, Effect<RepositoryEvent, State>> whenChecked(
        BiFunction<State, T, Effect<RepositoryEvent, State>> then) {

        return whenResponsible(whenAuthorized(then));
    }

    private <T extends RepositoryMessage> BiFunction<State, T, Effect<RepositoryEvent, State>> whenAuthorized(
        BiFunction<State, T, Effect<RepositoryEvent, State>> then) {

        return (state, message) -> state.authorizations
            .isAuthorized(message)
            .map(authorized -> {
                if (authorized) {
                    return then.apply(state, message);
                } else {
                    actor.getLog().warning(
                        "Refusing operation in repository '{}/{}' for user {}",
                        message.getNamespace().getValue(),
                        message.getRepository().getValue(),
                        message.getExecutor());

                    return Effect()
                        .none()
                        .thenRun(() -> message
                            .getErrorTo()
                            .tell(UserNotAuthorizedError.apply(message.getId(), message.getExecutor())));
                }
            })
            .orElseGet(() -> {
                actor.getLog().warning(
                    "No authorization result provided in repository '{}/{}' for operation {}",
                    message.getNamespace().getValue(),
                    message.getRepository().getValue(),
                    message);

                return Effect()
                    .none()
                    .thenRun(() -> message
                        .getErrorTo()
                        .tell(UserNotAuthorizedError.apply(message.getId(), message.getExecutor())));
            });
    }

    private <T extends RepositoryMessage> BiFunction<State, T, Effect<RepositoryEvent, State>> whenResponsible(
        BiFunction<State, T, Effect<RepositoryEvent, State>> then) {

        return (state, message) -> {
            if (message.getNamespace().equals(namespace) && message.getRepository().equals(name)) {
                return then.apply(state, message);
            } else {
                actor.getLog().warning(
                    "Ignoring message for repository '{}/{}'",
                    message.getNamespace().getValue(),
                    message.getRepository().getValue());

                return Effect().none();
            }
        };
    }

    @Data
    @AllArgsConstructor(staticName = "apply")
    protected static class State {

        private Map<RefSpec.VersionRef, VersionStatus> versions;

        private Map<RefSpec.TagRef, RefSpec.VersionRef> tags;

        private RepositoryAuthorizations authorizations;

        private Date created;

    }

}