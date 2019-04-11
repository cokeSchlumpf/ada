package ada.commons.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public final class Operators {

    private Operators() {

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

}
