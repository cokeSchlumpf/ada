package ada.commons.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

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
