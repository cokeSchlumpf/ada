package ada.vcs.client.converters;

import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.csv.javadsl.CsvParsing;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.reactivestreams.Publisher;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CSVReadResult implements ReadResult {

    private final ActorMaterializer materializer;

    private final Path file;

    @Override
    public Schema getSchema() {
        Flow<ByteString, List<String>, NotUsed> csv = CsvParsing
            .lineScanner()
            .map(line -> line.stream().map(ByteString::utf8String).collect(Collectors.toList()))
            .map(s -> s);

        FileIO
            .fromFile(file.toFile())
            .via(csv)
            .runWith(Sink.ignore(), materializer);
        return null;
    }

    @Override
    public Publisher<GenericRecord> getRecords() {
        return null;
    }

}
