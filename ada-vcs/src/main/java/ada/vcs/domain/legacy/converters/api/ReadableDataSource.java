package ada.vcs.domain.legacy.converters.api;

import ada.vcs.domain.legacy.repository.api.RefSpec;
import akka.stream.javadsl.Source;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface ReadableDataSource {

    String info();

    Schema schema();

    RefSpec.VersionRef ref();

    Source<GenericRecord, CompletionStage<ReadSummary>> getRecords(Monitor monitor);

}
