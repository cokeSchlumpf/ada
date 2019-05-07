package ada.commons.util;

import ada.commons.exceptions.AskCompletionException;
import ada.commons.exceptions.AskTimeoutException;
import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.RecipientRef;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.japi.Function;
import akka.japi.function.Function2;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class ActorPatterns {

    private static final long DEFAULT_TIMEOUT = 3;

    private final ActorSystem system;

    public <T, U> CompletionStage<U> ask(
        RecipientRef<T> actorRef, Function<ActorRef<U>, T> message) {

        return ask(actorRef, message, DEFAULT_TIMEOUT);
    }

    public <T, U> CompletionStage<U> ask(
        RecipientRef<T> actorRef, Function<ActorRef<U>, T> message, long durationInSeconds) {

        return ask(actorRef, (a1, ignore) -> message.apply(a1), durationInSeconds);
    }

    public <T, U, E extends ErrorMessage> CompletionStage<U> ask(
        RecipientRef<T> actorRef, Function2<ActorRef<U>, ActorRef<E>, T> message) {

        return ask(actorRef, message, DEFAULT_TIMEOUT);
    }

    public <T, U, E extends ErrorMessage> CompletionStage<U> ask(
        RecipientRef<T> actorRef, Function2<ActorRef<U>, ActorRef<E>, T> message, long durationInSeconds) {

        final String id = Operators.hash();

        final CompletableFuture<U> result = new CompletableFuture<>();
        final CompletableFuture<E> error = new CompletableFuture<>();
        final CompletableFuture<Done> timeout = new CompletableFuture<>();

        final Behavior<U> resultBehavior = Behaviors.receive((ctx, msg) -> {
            result.complete(msg);
            return Behaviors.same();
        });

        final Behavior<E> resultError = Behaviors.receive((ctx, msg) -> {
            error.complete(msg);
            return Behaviors.same();
        });

        final ActorRef<U> resultActor = Adapter.spawn(system, resultBehavior, String.format("%s-result", id));
        final ActorRef<E> errorActor = Adapter.spawn(system, resultError, String.format("%s-error", id));

        final T msg = Operators.suppressExceptions(() -> message.apply(resultActor, errorActor));

        actorRef.tell(msg);

        system.scheduler().scheduleOnce(
            Duration.ofSeconds(durationInSeconds),
            () -> timeout.complete(Done.getInstance()),
            system.dispatcher());

        error
            .thenAccept(e -> {
                if (!result.isDone() && !result.isCancelled()) {
                    result.completeExceptionally(AskCompletionException.apply(e));
                }
            });

        timeout
            .thenAccept(e -> {
                if (!result.isDone() && !result.isCancelled()) {
                    result.completeExceptionally(AskTimeoutException.apply());
                }
            });

        return result;
    }

}
