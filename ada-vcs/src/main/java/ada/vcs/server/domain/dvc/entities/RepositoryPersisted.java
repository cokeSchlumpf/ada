package ada.vcs.server.domain.dvc.entities;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryEvent;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.protocol.commands.GrantAccessToRepository;
import ada.vcs.server.domain.dvc.protocol.commands.Push;
import ada.vcs.server.domain.dvc.protocol.commands.RevokeAccessToRepository;
import ada.vcs.server.domain.dvc.protocol.errors.RefSpecAlreadyExistsError;
import ada.vcs.server.domain.dvc.protocol.errors.RefSpecNotFoundError;
import ada.vcs.server.domain.dvc.protocol.errors.UserNotAuthorizedError;
import ada.vcs.server.domain.dvc.protocol.events.GrantedAccessToRepository;
import ada.vcs.server.domain.dvc.protocol.events.RevokedAccessToRepository;
import ada.vcs.server.domain.dvc.protocol.queries.Pull;
import ada.vcs.server.domain.dvc.protocol.queries.RepositorySummaryRequest;
import ada.vcs.server.domain.dvc.protocol.queries.RepositorySummaryResponse;
import ada.vcs.server.domain.dvc.values.GrantedAuthorization;
import ada.vcs.server.domain.dvc.values.RepositoryAuthorizations;
import ada.vcs.server.domain.dvc.values.RepositorySummary;
import ada.vcs.shared.repository.api.RefSpec;
import ada.vcs.shared.repository.api.RepositorySinkMemento;
import ada.vcs.shared.repository.api.RepositorySourceMemento;
import ada.vcs.shared.repository.api.RepositoryStorageAdapter;
import ada.vcs.shared.repository.api.version.VersionDetails;
import ada.vcs.shared.repository.watcher.WatcherSinkMemento;
import ada.vcs.shared.repository.watcher.WatcherSourceMemento;
import akka.actor.typed.javadsl.ActorContext;
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
import java.util.Map;
import java.util.function.BiFunction;

public final class RepositoryPersisted extends EventSourcedBehavior<RepositoryMessage, RepositoryEvent, RepositoryPersisted.State> {

    private final ActorContext<RepositoryMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    public RepositoryPersisted(
        PersistenceId persistenceId, ActorContext<RepositoryMessage> actor, CommandContext context,
        RepositoryStorageAdapter repositoryStorageAdapter, ResourceName namespace, ResourceName name) {

        super(persistenceId);
        this.actor = actor;
        this.context = context;
        this.repositoryStorageAdapter = repositoryStorageAdapter;
        this.namespace = namespace;
        this.name = name;
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
            .onCommand(Pull.class, whenChecked(this::onPull))
            .onCommand(Push.class, whenChecked(this::onPush))
            .onCommand(RepositorySummaryRequest.class, whenResponsible(this::onSummaryRequest))
            .onCommand(RevokeAccessToRepository.class, whenChecked(this::onRevoke))
            .build();
    }

    @Override
    public EventHandler<State, RepositoryEvent> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(GrantedAccessToRepository.class, this::onGranted)
            .onEvent(RevokedAccessToRepository.class, this::onRevoked)
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

            state.versions.put(versionRef, details);
            push.getReplyTo().tell(watcherSinkMemento);
        } else {
            RefSpecAlreadyExistsError error = RefSpecAlreadyExistsError.apply(
                push.getId(),
                push.getNamespace(),
                push.getRepository(),
                versionRef);

            push.getErrorTo().tell(error);
        }

        return Effect().none();
    }

    private Effect<RepositoryEvent, State> onSummaryRequest(State state, RepositorySummaryRequest request) {
        RepositorySummary summary;

        boolean isAuthorized = state.authorizations.isAuthorized(request).orElse(false);
        if (isAuthorized) {
            if (state.versions.isEmpty()) {
                summary = RepositorySummary.apply(namespace, name, state.created);
            } else {
                VersionDetails details = Ordering
                    .natural()
                    .reverse()
                    .onResultOf(VersionDetails::date)
                    .sortedCopy(state.versions.values())
                    .get(0);

                summary = RepositorySummary.apply(namespace, name, details.date(), details.id());
            }

            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name, summary);
            request.getReplyTo().tell(response);
        } else {
            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name);
            request.getReplyTo().tell(response);
        }

        return Effect().none();
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


    /*
     * Helper functions
     */

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

        private Map<RefSpec.VersionRef, VersionDetails> versions;

        private Map<RefSpec.TagRef, RefSpec.VersionRef> tags;

        private RepositoryAuthorizations authorizations;

        private Date created;

    }

}
