package ada.vcs.client.converters.api;

import akka.stream.Materializer;
import org.apache.avro.Schema;

import java.util.concurrent.CompletionStage;

public interface DataSource<T extends Context> {

    CompletionStage<ReadableDataSource<T>> analyze(Materializer materializer, Schema schema);

    CompletionStage<ReadableDataSource<T>> analyze(Materializer materializer);

    String info();

    DataSourceMemento memento();

}
