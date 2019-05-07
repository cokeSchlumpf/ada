package ada.commons.util;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.RecipientRef;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class Operators {

    private Operators() {

    }

    public static <T, E extends Exception> CompletionStage<T> completeExceptionally(E with) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(with);
        return result;
    }

    public static <T> CompletionStage<T> completeExceptionally() {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(new RuntimeException());
        return result;
    }

    public static <T> Optional<T> exceptionToNone(ExceptionalSupplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static String hash() {
        return Hashing
            .goodFastHash(8)
            .newHasher()
            .putLong(System.currentTimeMillis())
            .hash()
            .toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> hasCause(Throwable t, Class<T> exType) {
        if (exType.isInstance(t)) {
            return Optional.of((T) t);
        } else if (t.getCause() != null) {
            return hasCause(t.getCause(), exType);
        } else {
            return Optional.empty();
        }
    }

    public static String extractMessage(Throwable ex) {
        return Optional
            .ofNullable(ExceptionUtils.getRootCause(ex))
            .map(t -> String.format("%s: %s", t.getClass().getSimpleName(), t.getMessage()))
            .orElse(Optional
                .ofNullable(ex.getMessage())
                .map(str -> String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage()))
                .orElse(String.format("%s: No details provided.", ex.getClass().getSimpleName())));
    }

    public static void suppressExceptions(ExceptionalRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public static <T> T suppressExceptions(ExceptionalSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    @FunctionalInterface
    public interface ExceptionalRunnable {

        void run() throws Exception;

    }

    @FunctionalInterface
    public interface ExceptionalSupplier<T> {

        T get() throws Exception;

    }

    @FunctionalInterface
    public interface ExceptionalFunction<I, R> {

        R apply(I in) throws Exception;

    }

}
