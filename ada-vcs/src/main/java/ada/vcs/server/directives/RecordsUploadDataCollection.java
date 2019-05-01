package ada.vcs.server.directives;

import ada.commons.util.Operators;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.japi.function.Function;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class RecordsUploadDataCollection<T> {

    public abstract CompletionStage<RecordsUploadDataCollection<T>> process(Function<VersionDetails, CompletionStage<T>> route);

    public abstract RecordsUploadDataCollection<T> withDetails(VersionDetails details);

    public abstract Optional<T> result();

    private RecordsUploadDataCollection() {

    }

    public static <T> RecordsUploadDataCollection<T> empty() {
        return Empty.apply();
    }

    @AllArgsConstructor(staticName = "apply")
    private static class Empty<T> extends RecordsUploadDataCollection<T> {


        @Override
        public CompletionStage<RecordsUploadDataCollection<T>> process(Function<VersionDetails, CompletionStage<T>> route) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public RecordsUploadDataCollection<T> withDetails(VersionDetails details) {
            return HasDetails.apply(details);
        }

        @Override
        public Optional<T> result() {
            return Optional.empty();
        }

    }

    @AllArgsConstructor(staticName = "apply")
    private static class HasDetails<T> extends RecordsUploadDataCollection<T> {

        private final VersionDetails details;


        @Override
        public CompletionStage<RecordsUploadDataCollection<T>> process(Function<VersionDetails, CompletionStage<T>> route) {
            return Operators.suppressExceptions(() -> route
                .apply(details)
                .thenApply(Processed::apply));
        }

        @Override
        public RecordsUploadDataCollection<T> withDetails(VersionDetails details) {
            return this;
        }

        @Override
        public Optional<T> result() {
            return Optional.empty();
        }

    }

    @AllArgsConstructor(staticName = "apply")
    private static class Processed<T> extends RecordsUploadDataCollection<T> {

        private final T result;


        @Override
        public CompletionStage<RecordsUploadDataCollection<T>> process(Function<VersionDetails, CompletionStage<T>> route) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public RecordsUploadDataCollection<T> withDetails(VersionDetails details) {
            return this;
        }

        @Override
        public Optional<T> result() {
            return Optional.of(result);
        }

    }

}
