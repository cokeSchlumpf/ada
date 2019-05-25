package ada.domain.dvc.entities;

import ada.commons.util.FQResourceName;
import ada.commons.util.ResourceName;
import ada.domain.dvc.entities.repository.State;
import ada.adapters.cli.commands.context.CommandContext;
import ada.domain.dvc.entities.repository.RepositoryUninitialized;
import ada.domain.dvc.protocol.api.RepositoryEvent;
import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.protocol.commands.*;
import ada.domain.dvc.protocol.events.*;
import ada.domain.dvc.protocol.errors.UserNotAuthorizedError;
import ada.domain.dvc.protocol.queries.Pull;
import ada.domain.dvc.protocol.queries.RepositoryDetailsRequest;
import ada.domain.dvc.protocol.queries.RepositorySummaryRequest;
import ada.domain.dvc.values.repository.RepositoryStorageAdapter;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class Repository extends EventSourcedEntity<RepositoryMessage, RepositoryEvent, State> {

    public static EntityTypeKey<RepositoryMessage> ENTITY_KEY = EntityTypeKey.create(RepositoryMessage.class, "repositories");

    private final ActorContext<RepositoryMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    public Repository(
        String entityId, ActorContext<RepositoryMessage> actor, CommandContext context,
        RepositoryStorageAdapter repositoryStorageAdapter, ResourceName namespace, ResourceName name) {

        super(ENTITY_KEY, entityId);
        this.actor = actor;
        this.context = context;
        this.repositoryStorageAdapter = repositoryStorageAdapter;
        this.namespace = namespace;
        this.name = name;
    }

    public static EventSourcedEntity<RepositoryMessage, RepositoryEvent, State> create(ActorContext<RepositoryMessage> actor, CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter,
                                                                                       ResourceName namespace, ResourceName name) {

        String entityId = createEntityId(namespace, name);
        return new Repository(entityId, actor, context, repositoryStorageAdapter, namespace, name);
    }

    public static Behavior<RepositoryMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter,
        ResourceName namespace, ResourceName name) {

        return Behaviors.setup(actor -> Repository.create(actor, context, repositoryStorageAdapter, namespace, name));
    }

    public static String createEntityId(ResourceName namespace, ResourceName repository) {
        return FQResourceName.apply(namespace, repository).toString();
    }

    @Override
    public State emptyState() {
        return RepositoryUninitialized.apply(actor, Effect(), context, repositoryStorageAdapter, namespace, name);
    }

    @Override
    public CommandHandler<RepositoryMessage, RepositoryEvent, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(
                CreateRepository.class,
                whenChecked(cmd(state -> state::onCreate)))
            .onCommand(
                GrantAccessToRepository.class,
                whenChecked(cmd(state -> state::onGrantAccess)))
            .onCommand(
                Pull.class,
                whenChecked(cmd(state -> state::onPull)))
            .onCommand(
                Push.class,
                whenChecked(cmd(state -> state::onPush)))
            .onCommand(
                RemoveRepository.class,
                whenChecked(cmd(state -> state::onRemove))
            )
            .onCommand(
                RepositoryDetailsRequest.class,
                whenChecked(cmd(state -> state::onDetailsRequest)))
            .onCommand(
                RepositorySummaryRequest.class,
                whenResponsible(cmd(state -> state::onSummaryRequest)))
            .onCommand(
                RevokeAccessFromRepository.class,
                whenChecked(cmd(state -> state::onRevokeAccess)))
            .onCommand(
                SubmitPushInRepository.class,
                whenChecked(cmd(state -> state::onSubmit)))
            .build();
    }

    @Override
    public EventHandler<State, RepositoryEvent> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(
                GrantedAccessToRepository.class,
                handle(state -> state::onGrantedAccess))
            .onEvent(
                RepositoryRemoved.class,
                handle(state -> state::onRemoved))
            .onEvent(
                RevokedAccessFromRepository.class,
                handle(state -> state::onRevokedAccess))
            .onEvent(
                RepositoryCreated.class,
                handle(state -> state::onCreated))
            .onEvent(
                VersionUpsertedInRepository.class,
                handle(state -> state::onUpserted))
            .build();
    }

    private <T extends RepositoryMessage> BiFunction<State, T, Effect<RepositoryEvent, State>> cmd(
        Function<State, Function<T, Effect<RepositoryEvent, State>>> func) {

        return (state, message) -> func.apply(state).apply(message);
    }

    private <T extends RepositoryEvent> BiFunction<State, T, State> handle(
        Function<State, Function<T, State>> func) {

        return (state, message) -> func.apply(state).apply(message);
    }

    private <T extends RepositoryMessage> BiFunction<State, T, Effect<RepositoryEvent, State>> whenChecked(
        BiFunction<State, T, Effect<RepositoryEvent, State>> then) {

        return whenResponsible(whenAuthorized(then));
    }

    private <T extends RepositoryMessage> BiFunction<State, T, Effect<RepositoryEvent, State>> whenAuthorized(
        BiFunction<State, T, Effect<RepositoryEvent, State>> then) {

        return (state, message) -> {
            if (state.isAuthorized(message)) {
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
        };
    }

    private <T extends RepositoryMessage> BiFunction<State, T, Effect<RepositoryEvent, State>> whenResponsible(
        BiFunction<State, T, Effect<RepositoryEvent, State>> then) {

        return (state, message) -> {
            if (message.getNamespace().equals(namespace) && message.getRepository().equals(name)) {
                return then.apply(state, message);
            } else {
                actor.getLog().warning(
                    "Ignoring message for repository '{}/{}' for '{}/{}': " + message,
                    message.getNamespace().getValue(),
                    message.getRepository().getValue(),
                    namespace, name);

                return Effect().none();
            }
        };
    }


}
