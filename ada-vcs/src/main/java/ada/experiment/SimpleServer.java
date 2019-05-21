package ada.experiment;

import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class SimpleServer extends HttpApp {

    private final ActorSystem system;

    @Override
    protected Route routes() {
        return pathPrefix(id -> {
            return concat(
                pathPrefix("increment", () -> {
                    getSharding().tell(new ShardingEnvelope<>("counter-" + id, new Increment()));
                    return complete("ok");
                }),
                pathPrefix("get", () -> {
                    CompletionStage<Integer> value = AskPattern.ask(
                        getSharding(),
                        (ActorRef<Integer> replyTo) -> new ShardingEnvelope<>("counter-" + id, GetValue.apply(replyTo)),
                        Duration.ofSeconds(3),
                        system.scheduler());

                    return onSuccess(value, v -> complete(v.toString()));
                }));
        });
    }

    private ActorRef<ShardingEnvelope<CounterCommand>> getSharding() {
        ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));

        EntityTypeKey<CounterCommand> typeKey = EntityTypeKey.create(CounterCommand.class, "Counter");

        return sharding.init(
            Entity.of(typeKey, ctx -> {
                System.out.println("Create entity with id '" + ctx.getEntityId());
                return counter(ctx.getEntityId(), 0);
            }));
    }


    public static Behavior<CounterCommand> counter(String entityId, Integer value) {
        return Behaviors.receive(CounterCommand.class)
            .onMessage(
                Increment.class,
                (ctx, msg) -> {
                    System.out.println("Increment " + entityId);
                    return counter(entityId, value + 1);
                })
            .onMessage(
                GetValue.class,
                (ctx, msg) -> {
                    msg.getReplyTo().tell(value);
                    return Behaviors.same();
                })
            .build();
    }

}
