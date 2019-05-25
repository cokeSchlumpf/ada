package ada.adapters.cli.converters.api;

import ada.domain.dvc.values.repository.RefSpec;
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
