package ada.vcs.client.converters.api;

import ada.vcs.client.core.repository.api.RefSpec;
import akka.stream.javadsl.Source;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface ReadableDataSource {

    Schema schema();

    RefSpec.VersionRef ref();

    Source<GenericRecord, CompletionStage<ReadSummary>> getRecords(Monitor monitor);

}
