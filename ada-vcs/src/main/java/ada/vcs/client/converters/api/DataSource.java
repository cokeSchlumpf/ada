package ada.vcs.client.converters.api;

import ada.vcs.client.core.FileSystemDependent;
import akka.stream.Materializer;
import org.apache.avro.Schema;

import java.util.concurrent.CompletionStage;

public interface DataSource extends FileSystemDependent<DataSource> {

    CompletionStage<ReadableDataSource> analyze(Materializer materializer, Schema schema);

    CompletionStage<ReadableDataSource> analyze(Materializer materializer);

    String info();

    DataSourceMemento memento();

}
