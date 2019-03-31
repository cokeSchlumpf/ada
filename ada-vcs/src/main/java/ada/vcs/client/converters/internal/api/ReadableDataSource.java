package ada.vcs.client.converters.internal.api;

import akka.stream.javadsl.Source;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

/**
 * Extension of {@link DataSource} which can be processed/ read.
 *
 * @param <T> See {@link DataSource}.
 */
public interface ReadableDataSource<T extends Context> extends DataSource<T> {

    /**
     * Should return whether the data source has changes based on the provided context.
     *
     * @param context The context which was produced by a previous import from the data source.
     * @return TRUE, when data-source has changed.
     */
    boolean hasDifference(T context);

    /**
     * @return the getSchema of the data source.
     */
    Schema getSchema();

    /**
     * Return source of records based on the provided context.
     *
     * @param monitor A monitor to notify about processing progress and warnings.
     * @param context The context which was created by a previous call to read records.
     * @return Read records from the data source.
     */
    Source<GenericRecord, CompletionStage<ReadSummary<T>>> getRecords(Monitor monitor, T context);

    /**
     * Return source of all records the data source provides.
     *
     * @param monitor A monitor to notify about processing progress and warnings.
     * @return Read records from the data source.
     */
    Source<GenericRecord, CompletionStage<ReadSummary<T>>> getRecords(Monitor monitor);

}
