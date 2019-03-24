package ada.vcs.client.converters;

import akka.Done;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor
public class CSVSource implements DataSource {

    private final Schema schema;

    @Override
    public Schema schema() {
        return schema;
    }

    @Override
    public Publisher<GenericRecord> records() {
        return null;
    }

}
