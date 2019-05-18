package ada.experiment;

import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AskPattern;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class SimpleServer extends HttpApp {

    private final ActorRef<ShardingEnvelope<CounterCommand>> sharding;

    private final ActorSystem system;

    @Override
    protected Route routes() {
        return pathPrefix(id -> {
            return concat(
                pathPrefix("increment", () -> {
                    sharding.tell(new ShardingEnvelope<>("counter-" + id, new Increment()));
                    return complete("ok");
                }),
                pathPrefix("get", () -> {
                    CompletionStage<Integer> value = AskPattern.ask(
                        sharding,
                        (ActorRef<Integer> replyTo) -> new ShardingEnvelope<>("counter-" + id, GetValue.apply(replyTo)),
                        Duration.ofSeconds(3),
                        system.scheduler());

                    return onSuccess(value, v -> complete(v.toString()));
                }));
        });
    }

}
