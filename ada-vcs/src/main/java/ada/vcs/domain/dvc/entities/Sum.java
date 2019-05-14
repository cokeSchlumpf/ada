package ada.vcs.domain.dvc.entities;

import ada.commons.databind.MessageSerializer;
import akka.actor.ExtendedActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.Map;

public final class Sum extends EventSourcedBehavior<Sum.Command, Sum.Event, Sum.State> {

    private final ActorContext<Command> actor;

    private Sum(ActorContext<Command> actor, PersistenceId persistenceId) {
        super(persistenceId);
        this.actor = actor;
    }

    public static Behavior<Command> createBehavior() {
        return Behaviors.setup(ctx -> new Sum(ctx, new PersistenceId("sum")));
    }

    @Override
    public State emptyState() {
        return State.apply(0);
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(Add.class, add -> {
                actor.getLog().info("Received Add command {}", add);
                return Effect()
                    .persist(Added.apply(add.getA()))
                    .thenRun(state -> actor.getLog().info("Do some side effect with state {}", state));
            })
            .build();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(Added.class, (state, added) -> {
                actor.getLog().info("Received Added event {}", added);
                state.setSum(state.getSum() + added.getA());
                actor.getLog().info("New state {}", state);

                return state;
            })
            .build();
    }

    public interface Command {}

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static class Add implements Command {

        private final int a;

    }

    interface Event {

    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Added implements Event {

        private final int a;

        @JsonCreator
        public static Added apply(@JsonProperty("a") int a) {
            return new Added(a);
        }

    }

    @Data
    @AllArgsConstructor(staticName = "apply")
    public static class State {

        private int sum;

    }

    public static class SumMessageSerializer extends MessageSerializer {

        public SumMessageSerializer(ExtendedActorSystem actorSystem) {
            super(actorSystem, 2403);
        }

        @Override
        protected Map<String, Class<?>> getManifestToClass() {
            Map<String, Class<?>> m = Maps.newHashMap();

            m.put("added/v1", Added.class);

            return m;
        }
    }

}
