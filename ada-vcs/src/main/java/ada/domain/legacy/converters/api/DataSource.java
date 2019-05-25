package ada.domain.legacy.converters.api;

import ada.commons.io.FileSystemDependent;
import akka.stream.Materializer;
import org.apache.avro.Schema;

import java.util.concurrent.CompletionStage;

public interface DataSource extends FileSystemDependent<DataSource> {

    CompletionStage<ReadableDataSource> analyze(Materializer materializer, Schema schema);

    CompletionStage<ReadableDataSource> analyze(Materializer materializer);

    String info();

    DataSourceMemento memento();

}
