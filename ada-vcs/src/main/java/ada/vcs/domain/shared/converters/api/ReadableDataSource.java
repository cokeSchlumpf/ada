package ada.vcs.domain.shared.converters.api;

import ada.vcs.domain.shared.repository.api.RefSpec;
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
