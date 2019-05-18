package ada.experiment;

import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;

public class ClusterApplication {

    public static void main(String... args) {
        ActorSystem system = ActorSystem.create();

        ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));

        EntityTypeKey<CounterCommand> typeKey = EntityTypeKey.create(CounterCommand.class, "Counter");

        ActorRef<ShardingEnvelope<CounterCommand>> shardRegion = sharding.init(
            Entity.of(typeKey, ctx -> {
                System.out.println("Create entity with id '" + ctx.getEntityId());
                return counter(ctx.getEntityId(), 0);
            }));

        SimpleServer server = SimpleServer.apply(shardRegion, system);

        try {
            server.startServer("0.0.0.0", nextFreePort(8080), system);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public static int nextFreePort(int from) {
        int port = from;
        while (true) {
            if (isLocalPortFree(port)) {
                return port;
            } else {
                port = nextFreePort(from + 1);
            }
        }
    }

    private static boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
