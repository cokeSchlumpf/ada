package ada.vcs.server.directives;

import ada.commons.util.Operators;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.http.javadsl.server.Route;
import akka.japi.function.Function;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

abstract class RecordsUploadDataCollection {

    public abstract CompletionStage<RecordsUploadDataCollection> process(Function<VersionDetails, CompletionStage<Route>> route);

    public abstract RecordsUploadDataCollection withDetails(VersionDetails details);

    public abstract Optional<Route> route();

    private RecordsUploadDataCollection() {

    }

    public static RecordsUploadDataCollection empty() {
        return Empty.apply();
    }

    @AllArgsConstructor(staticName = "apply")
    private static class Empty extends RecordsUploadDataCollection {


        @Override
        public CompletionStage<RecordsUploadDataCollection> process(Function<VersionDetails, CompletionStage<Route>> route) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public RecordsUploadDataCollection withDetails(VersionDetails details) {
            return HasDetails.apply(details);
        }

        @Override
        public Optional<Route> route() {
            return Optional.empty();
        }

    }

    @AllArgsConstructor(staticName = "apply")
    private static class HasDetails extends RecordsUploadDataCollection {

        private final VersionDetails details;


        @Override
        public CompletionStage<RecordsUploadDataCollection> process(Function<VersionDetails, CompletionStage<Route>> route) {
            return Operators.suppressExceptions(() -> route
                .apply(details)
                .thenApply(Processed::apply));
        }

        @Override
        public RecordsUploadDataCollection withDetails(VersionDetails details) {
            return this;
        }

        @Override
        public Optional<Route> route() {
            return Optional.empty();
        }

    }

    @AllArgsConstructor(staticName = "apply")
    private static class Processed extends RecordsUploadDataCollection {

        private final Route route;


        @Override
        public CompletionStage<RecordsUploadDataCollection> process(Function<VersionDetails, CompletionStage<Route>> route) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public RecordsUploadDataCollection withDetails(VersionDetails details) {
            return this;
        }

        @Override
        public Optional<Route> route() {
            return Optional.of(route);
        }

    }

}
