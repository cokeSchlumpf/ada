package ada.vcs.domain.dvc.entities;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlEvent;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.vcs.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.domain.dvc.protocol.commands.RemoveNamespace;
import ada.vcs.domain.dvc.protocol.errors.NamespaceNotFound;
import ada.vcs.domain.dvc.protocol.events.NamespaceCreated;
import ada.vcs.domain.dvc.protocol.events.NamespaceRemoved;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesRequest;
import ada.vcs.domain.dvc.services.repositories.RepositoriesQuery;
import ada.vcs.domain.dvc.services.repositories.StartRepositoriesQuery;
import ada.vcs.domain.legacy.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.HashMap;

public final class DataVersionControl
    extends EventSourcedBehavior<DataVersionControlMessage, DataVersionControlEvent, DataVersionControl.State> {

    private final ActorContext<DataVersionControlMessage> actor;

    private final CommandContext context;

    private final ActorRef<ShardingEnvelope<NamespaceMessage>> namespaceShards;

    private final ActorRef<ShardingEnvelope<RepositoryMessage>> repositoryShards;

    public DataVersionControl(
        PersistenceId persistenceId, ActorContext<DataVersionControlMessage> actor, CommandContext context,
        ActorRef<ShardingEnvelope<NamespaceMessage>> namespaceShards,
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositoryShards) {

        super(persistenceId);
        this.actor = actor;
        this.context = context;
        this.namespaceShards = namespaceShards;
        this.repositoryShards = repositoryShards;
    }

    public static Behavior<DataVersionControlMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter) {
        PersistenceId pid = PersistenceId.apply("dvc");
        return Behaviors.setup(actor -> {
            final ClusterSharding sharding = ClusterSharding.get(actor.getSystem());

            final Entity<RepositoryMessage, ShardingEnvelope<RepositoryMessage>> repoEntity = Entity
                .ofPersistentEntity(Repository.ENTITY_KEY, ctx -> {
                    FQResourceName repoName = FQResourceName.apply(ctx.getEntityId());

                    return Repository.create(
                        ctx.getActorContext(), context, repositoryStorageAdapter,
                        repoName.getNamespace(), repoName.getName());
                });

            final ActorRef<ShardingEnvelope<RepositoryMessage>> repositoryShards = sharding.init(repoEntity);

            final Entity<NamespaceMessage, ShardingEnvelope<NamespaceMessage>> nsEntity = Entity
                .ofPersistentEntity(Namespace.ENTITY_KEY, ctx -> {
                    ResourceName name = ResourceName.apply(ctx.getEntityId());

                    return Namespace.createEntity(ctx.getActorContext(), context, name, repositoryShards);
                });

            final ActorRef<ShardingEnvelope<NamespaceMessage>> namespaceShards = sharding.init(nsEntity);

            return new DataVersionControl(pid, actor, context, namespaceShards, repositoryShards);
        });
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
            Namespace.createBehavior(context, created.getNamespace(), repositoryShards),
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
