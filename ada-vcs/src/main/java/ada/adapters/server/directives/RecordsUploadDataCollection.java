package ada.adapters.server.directives;

import ada.commons.util.Operators;
import ada.domain.dvc.values.repository.version.VersionDetails;
import akka.japi.function.Function;
import lombok.AllArgsConstructor;

import java.util.Optional;

public abstract class RecordsUploadDataCollection<T> {

    public abstract RecordsUploadDataCollection<T> process(Function<VersionDetails, T> route);

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
        public RecordsUploadDataCollection<T> process(Function<VersionDetails, T> route) {
            return this;
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
        public RecordsUploadDataCollection<T> process(Function<VersionDetails, T> route) {
            return Processed.apply(Operators.suppressExceptions(() -> route.apply(details)));
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
        public RecordsUploadDataCollection<T> process(Function<VersionDetails, T> route) {
            return this;
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
