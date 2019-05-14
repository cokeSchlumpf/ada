package ada.vcs.domain.dvc;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.entities.Namespace;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.vcs.domain.dvc.protocol.commands.RemoveNamespace;
import ada.vcs.domain.dvc.protocol.events.NamespaceRemoved;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesRequest;
import ada.vcs.domain.dvc.services.RepositoriesQuery;
import ada.vcs.domain.dvc.services.StartRepositoriesQuery;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlEvent;
import ada.vcs.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.domain.dvc.protocol.errors.NamespaceNotFound;
import ada.vcs.domain.dvc.protocol.events.NamespaceCreated;
import ada.vcs.domain.legacy.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.HashMap;

public final class DataVersionControlPersisted
    extends EventSourcedBehavior<DataVersionControlMessage, DataVersionControlEvent, DataVersionControlPersisted.State> {

    private final ActorContext<DataVersionControlMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    public DataVersionControlPersisted(
        PersistenceId persistenceId, ActorContext<DataVersionControlMessage> actor, CommandContext context,
        RepositoryStorageAdapter repositoryStorageAdapter) {

        super(persistenceId);
        this.actor = actor;
        this.context = context;
        this.repositoryStorageAdapter = repositoryStorageAdapter;
    }

    public static Behavior<DataVersionControlMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter) {

        PersistenceId pid = PersistenceId.apply("dvc");
        return Behaviors.setup(ctx -> new DataVersionControlPersisted(pid, ctx, context, repositoryStorageAdapter));
    }

    @Override
    public State emptyState() {
        return State.apply(Maps.newHashMap());
    }

    @Override
    public CommandHandler<DataVersionControlMessage, DataVersionControlEvent, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(CreateRepository.class, this::onCreateRepository)
            .onCommand(NamespaceMessage.class, this::forward)
            .onCommand(RemoveNamespace.class, this::onRemoveNamespace)
            .onCommand(RepositoriesRequest.class, this::onRepositoriesRequest)
            .build();
    }

    @Override
    public EventHandler<State, DataVersionControlEvent> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(NamespaceCreated.class, this::onNamespaceCreated)
            .onEvent(NamespaceRemoved.class, this::onNamespaceRemoved)
            .build();
    }

    private Effect<DataVersionControlEvent, State> onCreateRepository(State state, CreateRepository create) {
        ActorRef<NamespaceMessage> namespaceActor = state.namespaceToActorRef.get(create.getNamespace());

        NamespaceCreated created = NamespaceCreated.apply(
            create.getId(), create.getNamespace(), create.getExecutor().getUserId(), new Date());

        if (namespaceActor == null) {
            return Effect()
                .persist(created)
                .thenRun(newState -> newState
                    .namespaceToActorRef
                    .get(create.getNamespace())
                    .tell(create));
        } else {
            namespaceActor.tell(create);
            return Effect().none();
        }
    }

    private Effect<DataVersionControlEvent, State> forward(State state, NamespaceMessage msg) {
        ActorRef<NamespaceMessage> namespaceActor = state.namespaceToActorRef.get(msg.getNamespace());

        if (namespaceActor != null) {
            namespaceActor.tell(msg);
        } else {
            actor.getLog().warning("Ignoring message for not existing namespace '{}'", msg.getNamespace().getValue());
            msg.getErrorTo().tell(NamespaceNotFound.apply(msg.getId(), msg.getNamespace()));
        }

        return Effect().none();
    }

    private State onNamespaceCreated(State state, NamespaceCreated created) {
        actor.getLog().info("Creating repository namespace '{}'", created.getNamespace().getValue());

        ActorRef<NamespaceMessage> namespaceActor = actor.spawn(
            Namespace.createBehavior(context, repositoryStorageAdapter, created.getNamespace()),
            created.getNamespace().getValue());

        state.namespaceToActorRef.put(created.getNamespace(), namespaceActor);

        actor.watchWith(
            namespaceActor,
            RemoveNamespace.apply(created.getId(), created.getNamespace(), actor.getSystem().deadLetters()));

        return state;
    }

    private Effect<DataVersionControlEvent, State> onRemoveNamespace(State state, RemoveNamespace remove) {
        NamespaceRemoved removed = NamespaceRemoved.apply(remove.getNamespace());
        if (state.namespaceToActorRef.containsKey(remove.getNamespace())) {
            return Effect().persist(removed);
        } else {
            return Effect().none();
        }
    }

    private State onNamespaceRemoved(State state, NamespaceRemoved removed) {
        state.namespaceToActorRef.remove(removed.getNamespace());
        return state;
    }


    private Effect<DataVersionControlEvent, State> onRepositoriesRequest(State state, RepositoriesRequest request) {
        final Behavior<Object> behavior = RepositoriesQuery.createBehavior();
        final ActorRef<Object> query = actor.spawn(behavior, String.format("repositories-query-%s", Operators.hash()));

        query.tell(StartRepositoriesQuery.apply(request.getExecutor(), request.getReplyTo(), state.namespaceToActorRef));
        return Effect().none();
    }

    @AllArgsConstructor(staticName = "apply")
    public static final class State {

        private HashMap<ResourceName, ActorRef<NamespaceMessage>> namespaceToActorRef;

    }

}
