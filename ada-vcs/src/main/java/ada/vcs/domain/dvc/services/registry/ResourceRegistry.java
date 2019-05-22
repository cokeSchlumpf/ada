package ada.vcs.domain.dvc.services.registry;

import ada.commons.util.ResourcePath;
import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.ORSet;
import akka.cluster.ddata.ORSetKey;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.typed.SingletonActor;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.util.Set;

public final class ResourceRegistry extends EventSourcedBehavior<ResourceRegistryCommand, ResourceRegistryEvent, ResourceRegistry.State> {

    public static final Key<ORSet<ResourcePath>> DD_REPOSITORIES_KEY = ORSetKey.create("repositories");

    private final ActorContext<ResourceRegistryCommand> actor;

    private final SelfUniqueAddress node;

    private final ActorRef<Replicator.Command> replicator;

    private ResourceRegistry(ActorContext<ResourceRegistryCommand> actor, SelfUniqueAddress node, ActorRef<Replicator.Command> replicator) {
        super(PersistenceId.apply("resource-registry"));
        this.actor = actor;
        this.node = node;
        this.replicator = replicator;
    }

    public static SingletonActor<ResourceRegistryCommand> createSingleton() {
        Behavior<ResourceRegistryCommand> behavior = Behaviors.setup(actor -> {
            final ActorRef<Replicator.Command> replicator = DistributedData.get(actor.getSystem()).replicator();
            final SelfUniqueAddress node = DistributedData.get(actor.getSystem()).selfUniqueAddress();

            return new ResourceRegistry(actor, node, replicator);
        });

        return SingletonActor.apply(behavior, "resource-registry");
    }

    @Override
    public State emptyState() {
        return State.apply(Sets.newHashSet());
    }

    @Override
    public CommandHandler<ResourceRegistryCommand, ResourceRegistryEvent, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(RegisterResource.class, this::onRegister)
            .onCommand(RemoveResource.class, this::onRemove)
            .build();
    }

    @Override
    public EventHandler<State, ResourceRegistryEvent> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(ResourceRegistered.class, this::onRegistered)
            .onEvent(ResourceRemoved.class, this::onRemoved)
            .build();
    }

    private Effect<ResourceRegistryEvent, State> onRegister(State state, RegisterResource register) {
        if (state.resources.contains(register.getResource())) {
            register.getReplyTo().tell(false);
            return Effect().none();
        } else {
            return Effect()
                .persist(ResourceRegistered.apply(register.getResource()))
                .thenRun(() -> register.getReplyTo().tell(true));
        }
    }

    @SuppressWarnings("unchecked")
    private State onRegistered(State state, ResourceRegistered registered) {
        final ActorRef<Object> updateActorRef = createUpdateResponseReceiver();

        final Replicator.Update<ORSet<ResourcePath>> updateMessage = new Replicator.Update<>(
            DD_REPOSITORIES_KEY,
            ORSet.empty(),
            Replicator.writeLocal(),
            updateActorRef.unsafeUpcast(),
            set -> set.add(node, registered.getResource()));

        replicator.tell(updateMessage);
        state.resources.add(registered.getResource());

        return state;
    }

    private Effect<ResourceRegistryEvent, State> onRemove(State state, RemoveResource remove) {
        if (state.resources.contains(remove.getResource())) {
            remove.getReplyTo().tell(Done.getInstance());
            return Effect().none();
        } else {
            return Effect()
                .persist(ResourceRemoved.apply(remove.getResource()))
                .thenRun(() -> remove.getReplyTo().tell(Done.getInstance()));
        }
    }

    private State onRemoved(State state, ResourceRemoved removed) {
        final ActorRef<Object> updateActorRef = createUpdateResponseReceiver();

        final Replicator.Update<ORSet<ResourcePath>> updateMessage = new Replicator.Update<>(
            DD_REPOSITORIES_KEY,
            ORSet.empty(),
            Replicator.writeLocal(),
            updateActorRef.unsafeUpcast(),
            set -> set.remove(node, removed.getResource()));

        replicator.tell(updateMessage);
        state.resources.remove(removed.getResource());

        return state;
    }

    private ActorRef<Object> createUpdateResponseReceiver() {
        return actor.spawnAnonymous(
            Behaviors.withTimers(timers -> {
                timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(10));

                return Behaviors
                    .receive(Object.class)
                    .onMessage(Timeout.class, (ctx, timeout) -> {
                        ctx.getLog().warning("Did not receive UpdateResponse from " + DD_REPOSITORIES_KEY + " CRDT.");
                        return Behaviors.stopped();
                    })
                    .onMessage(Replicator.UpdateResponse.class, (ctx, response) -> Behaviors.stopped())
                    .build();
            }));
    }

    @AllArgsConstructor(staticName = "apply")
    public static class State {

        private Set<ResourcePath> resources;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class Timeout {

    }

}
