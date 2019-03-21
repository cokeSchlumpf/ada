package ada.vcs.client.converters;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.reactivestreams.Publisher;

public interface ReadResult {

    Schema getSchema();

    Publisher<GenericRecord> getRecords();

}
