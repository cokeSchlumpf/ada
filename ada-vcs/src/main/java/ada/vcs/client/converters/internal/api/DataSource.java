package ada.vcs.client.converters.internal.api;

import akka.stream.Materializer;

import java.util.concurrent.CompletionStage;

/**
 * A generic data source interface.
 *
 * @param <T> The type of the context the data source uses to store processing result information.
 */
public interface DataSource<T extends Context> {

    /**
     * Analyzes the data within the data source, e.g. to extract missing properties from the data source.
     *
     * @return An enriched instance of the data source which can be executed/ read.
     */
    CompletionStage<ReadableDataSource<T>> analyze(Materializer materializer);

}
