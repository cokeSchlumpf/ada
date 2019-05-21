package ada.vcs.domain.dvc.entities;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.protocol.api.NamespaceEvent;
import ada.vcs.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.domain.dvc.protocol.errors.RepositoryNotFoundError;
import ada.vcs.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.domain.dvc.protocol.events.RepositoryRemoved;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesInNamespaceRequest;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesInNamespaceResponse;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;
import java.util.HashSet;
import java.util.function.BiFunction;

public final class Namespace extends EventSourcedEntity<NamespaceMessage, NamespaceEvent, Namespace.State> {

    public static EntityTypeKey<NamespaceMessage> ENTITY_KEY = EntityTypeKey.create(NamespaceMessage.class, "namespaces");

    private final ActorContext<NamespaceMessage> actor;

    private final ActorRef<ShardingEnvelope<RepositoryMessage>> repositoryShards;

    private final ResourceName name;

    public Namespace(
        String entityId, ActorContext<NamespaceMessage> actor, ResourceName name,
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositoryShards) {

        super(ENTITY_KEY, entityId);
        this.actor = actor;
        this.name = name;
        this.repositoryShards = repositoryShards;
    }

    public static EventSourcedEntity<NamespaceMessage, NamespaceEvent, Namespace.State> createEntity(
        ActorContext<NamespaceMessage> actor, CommandContext context, ResourceName name,
        ActorRef<ShardingEnvelope<RepositoryMessage>> shards) {

        final String entityId = createEntityId(name);
        return new Namespace(entityId, actor, name, shards);
    }

    public static Behavior<NamespaceMessage> createBehavior(
        CommandContext context, ResourceName name, ActorRef<ShardingEnvelope<RepositoryMessage>> shards) {

        final String entityId = createEntityId(name);
        return Behaviors.setup(actor -> new Namespace(entityId, actor, name, shards));
    }

    public static String createEntityId(ResourceName namespace) {
        return namespace.toString();
    }

    @Override
    public State emptyState() {
        return State.apply(Sets.newHashSet());
    }

    @Override
    public CommandHandler<NamespaceMessage, NamespaceEvent, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(CreateRepository.class, whenResponsible(this::onCreateRepository))
            .onCommand(RepositoriesInNamespaceRequest.class, whenResponsible(this::onRepositoriesRequest))
            .onCommand(RepositoryTerminated.class, whenResponsible(this::onRepositoryTerminated))
            .onCommand(RepositoryMessage.class, whenResponsible(this::forward))
            .build();
    }

    @Override
    public EventHandler<State, NamespaceEvent> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(RepositoryCreated.class, this::onRepositoryCreated)
            .onEvent(RepositoryRemoved.class, this::onRepositoryRemoved)
            .build();
    }

    private Effect<NamespaceEvent, State> onCreateRepository(State state, CreateRepository create) {
        ShardingEnvelope<RepositoryMessage> msg = new ShardingEnvelope<>(
            Repository.createEntityId(name, create.getRepository()), create);

        if (!state.repositories.contains(create.getRepository())) {
            RepositoryCreated created = RepositoryCreated.apply(
                create.getId(), create.getNamespace(), create.getRepository(),
                create.getExecutor().getUserId(), new Date());

            return Effect()
                .persist(created)
                .thenRun(() -> repositoryShards.tell(msg));
        } else {
            repositoryShards.tell(msg);
            return Effect().none();
        }
    }

    private State onRepositoryCreated(State state, RepositoryCreated created) {
        actor.getLog().info(
            "Creating repository '{}/{}'",
            created.getNamespace().getValue(), created.getRepository().getValue());

        state.repositories.add(created.getRepository());

        return state;
    }

    private Effect<NamespaceEvent, State> forward(State state, RepositoryMessage msg) {
        if (state.repositories.contains(msg.getRepository())) {
            repositoryShards.tell(new ShardingEnvelope<>(Repository.createEntityId(name, msg.getRepository()), msg));
        } else {
            actor.getLog().warning(
                "Ignoring message for not existing repository '{}/{}'",
                msg.getNamespace().getValue(), msg.getRepository().getValue());

            RepositoryNotFoundError error = RepositoryNotFoundError.apply(
                msg.getId(), msg.getNamespace(), msg.getRepository());

            msg.getErrorTo().tell(error);
        }

        return Effect().none();
    }

    private Effect<NamespaceEvent, State> onRepositoriesRequest(State state, RepositoriesInNamespaceRequest request) {
        RepositoriesInNamespaceResponse response = RepositoriesInNamespaceResponse
            .apply(name, Sets.newHashSet(state.repositories));

        request.getReplyTo().tell(response);
        return Effect().none();
    }

    private State onRepositoryRemoved(State state, RepositoryRemoved removed) {
        actor.getLog().info(
            "Repository '{}/{}' has been removed", removed.getNamespace(), removed.getRepository());

        state.repositories.remove(removed.getRepository());

        return state;
    }

    private Effect<NamespaceEvent, State> onRepositoryTerminated(State state, RepositoryTerminated terminated) {
        if (state.repositories.contains(terminated.getRepository())) {
            return Effect()
                .persist(RepositoryRemoved.apply(terminated.namespace, terminated.repository));
        } else {
            return Effect().none();
        }
    }

    private <T extends NamespaceMessage> BiFunction<State, T, Effect<NamespaceEvent, State>> whenResponsible(
        BiFunction<State, T, Effect<NamespaceEvent, State>> then) {

        return (state, message) -> {
            if (message.getNamespace().equals(name)) {
                return then.apply(state, message);
            } else {
                actor.getLog().warning(
                    "Ignoring message for namespace '{}'",
                    message.getNamespace().getValue());

                return Effect().none();
            }
        };
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RepositoryTerminated implements NamespaceMessage {

        private final String id;

        public final ResourceName namespace;

        public final ResourceName repository;

        public final ActorRef<ErrorMessage> errorTo;

    }

    @AllArgsConstructor(staticName = "apply")
    public static class State {

        private HashSet<ResourceName> repositories;

    }

}
