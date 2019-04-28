package ada.vcs.server.directives;

import ada.commons.util.Operators;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.util.Optional;

abstract class FormDataCollector {

    public abstract FormDataCollector withData(Source<ByteString, ?> records);

    public abstract FormDataCollector withDetails(VersionDetails details);

    public abstract <T> Optional<T> map(Function2<VersionDetails, Source<ByteString, ?>, T> f);

    private FormDataCollector() {

    }

    public static FormDataCollector empty() {
        return Empty.apply();
    }

    @AllArgsConstructor(staticName = "apply")
    private static class Empty extends FormDataCollector {

        @Override
        public FormDataCollector withData(Source<ByteString, ?> records) {
            return this;
        }

        @Override
        public FormDataCollector withDetails(VersionDetails details) {
            return HasDetails.apply(details);
        }

        @Override
        public <T> Optional<T> map(Function2<VersionDetails, Source<ByteString, ?>, T> f) {
            return Optional.empty();
        }

    }

    @AllArgsConstructor(staticName = "apply")
    private static class HasDetails extends FormDataCollector {

        private final VersionDetails details;

        @Override
        public FormDataCollector withData(Source<ByteString, ?> records) {
            return Complete.apply(details, records);
        }

        @Override
        public FormDataCollector withDetails(VersionDetails details) {
            return HasDetails.apply(details);
        }

        @Override
        public <T> Optional<T> map(Function2<VersionDetails, Source<ByteString, ?>, T> f) {
            return Optional.empty();
        }

    }

    @AllArgsConstructor(staticName = "apply")
    private static class Complete extends FormDataCollector {

        private final VersionDetails details;

        private final Source<ByteString, ?> source;

        @Override
        public FormDataCollector withData(Source<ByteString, ?> records) {
            return Complete.apply(details, records);
        }

        @Override
        public FormDataCollector withDetails(VersionDetails details) {
            return Complete.apply(details, source);
        }

        @Override
        public <T> Optional<T> map(Function2<VersionDetails, Source<ByteString, ?>, T> f) {
            return Optional.of(Operators.suppressExceptions(() -> f.apply(details, source)));
        }
    }

}
