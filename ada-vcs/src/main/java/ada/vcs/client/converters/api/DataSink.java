package ada.vcs.client.converters.api;

import akka.stream.javadsl.Sink;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface DataSink {

    Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema);

    String info();

    DataSinkMemento memento();

}
