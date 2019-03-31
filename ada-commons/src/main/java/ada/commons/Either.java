package ada.commons;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Either<L, R> {

    private Either() {
    }

    public static <L, R> Either<L, R> left(L left) {
        return Left.apply(left);
    }

    public static <L, R> Either<L, R> right(R right) {
        return Right.apply(right);
    }

    public static <L, R> Either<L, R> result(ThrowableSupplier<L> sup, Function<Exception, R> onFailure) {
        try {
            return left(sup.get());
        } catch (Exception e) {
            return right(onFailure.apply(e));
        }
    }

    public static <L> Either<L, Exception> result(ThrowableSupplier<L> sup) {
        try {
            return left(sup.get());
        } catch (Exception e) {
            return right(e);
        }
    }

    public abstract <T> T map(Function<? super L, ? extends T> lFunc, Function<? super R, ? extends T> rFunc);

    @SuppressWarnings("unchecked")
    public <T> Either<T, R> mapLeft(Function<? super L, ? extends T> lFunc) {
        return this.map(t -> Left.apply(lFunc.apply(t)), t -> (Either<T, R>) this);
    }

    @SuppressWarnings("unchecked")
    public <T> Either<L, T> mapRight(Function<? super R, ? extends T> lFunc) {
        return this.map(t -> (Either<L, T>) this, t -> Right.apply(lFunc.apply(t)));
    }

    public void apply(Consumer<? super L> lFunc, Consumer<? super R> rFunc) {
        map(consume(lFunc), consume(rFunc));
    }

    private <T> Function<T, Void> consume(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return null;
        };
    }

    public abstract boolean isLeft();

    public abstract boolean isRight();

    @Value
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor(staticName = "apply")
    public static class Left<L, R> extends Either<L, R> {

        private L left;

        @Override
        public <T> T map(Function<? super L, ? extends T> lFunc, Function<? super R, ? extends T> rFunc) {
            return lFunc.apply(left);
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor(staticName = "apply")
    public static class Right<L, R> extends Either<L, R> {

        private R right;

        @Override
        public <T> T map(Function<? super L, ? extends T> lFunc, Function<? super R, ? extends T> rFunc) {
            return rFunc.apply(right);
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {

        T get() throws Exception;

    }

}
