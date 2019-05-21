package ada.vcs.domain.dvc.services.registry;

import ada.commons.util.ResourcePath;
import akka.Done;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;

import java.util.Set;

public final class ResourceRegistry extends EventSourcedBehavior<ResourceRegistryCommand, ResourceRegistryEvent, ResourceRegistry.State> {

    private ResourceRegistry() {
        super(PersistenceId.apply("resource-registry"));
    }

    public static Behavior<ResourceRegistryCommand> createBehavior() {
        return Behaviors.setup(actor -> new ResourceRegistry());
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

    private State onRegistered(State state, ResourceRegistered registered) {
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
        state.resources.remove(removed.getResource());
        return state;
    }

    @AllArgsConstructor(staticName = "apply")
    public static class State {

        private final Set<ResourcePath> resources;

    }

}
