package ada.vcs.client.converters;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface RecordSource {

    CompletionStage<Schema> schema(int analyzeRecordsCount);

    Source<GenericRecord, NotUsed> source();

}
