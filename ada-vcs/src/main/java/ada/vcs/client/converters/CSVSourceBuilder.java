package ada.vcs.client.converters;

import ada.commons.NameFactory;
import ada.vcs.client.datatypes.DataType;
import ada.vcs.client.datatypes.DataTypeDetector;
import akka.Done;
import akka.NotUsed;
import akka.japi.function.Function;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.alpakka.csv.javadsl.CsvParsing;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply")
public class CSVSourceBuilder {

    private final Path file;

    private final List<String> headers;

    private final char fieldSeparator;

    private final char quoteChar;

    private final char escapeChar;

    private final Materializer materializer;

    public CompletionStage<Done> get(boolean incremental) {
        return schema()
            .thenApply(i -> {
                System.out.println(i.toString(true));
                return Done.getInstance();
            });
    }

    private CompletionStage<Schema> schema() {
        return headers()
            .thenCompose(headers -> stream()
                .drop(1) // ignore header
                .take(100)
                .grouped(100)
                .runWith(Sink.head(), materializer)
                .thenApply(samples -> {
                    DataTypeDetector[] detectors = new DataTypeDetector[headers.size()];
                    DataType[] types = new DataType[headers.size()];

                    for (int i = 0; i < headers.size(); i++) {
                        detectors[i] = DataTypeDetector.apply();
                    }

                    for (List<String> sample : samples) {
                        for (int i = 0; i < headers.size() && i < sample.size(); i++) {
                            detectors[i].hint(sample.get(i));
                        }
                    }

                    NameFactory nf = NameFactory.apply(NameFactory.Defaults.LOWERCASE_UNDERSCORED);

                    SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder
                        .record(nf.create(FilenameUtils.removeExtension(file.getFileName().toString())))
                        .namespace("ada.autodetect")
                        .fields();

                    for (int i = 0; i < headers.size(); i++) {
                        System.out.println(String.format("%s: %s - %s", headers.get(i), detectors[i].type().name(), detectors[i].proximity()));
                        types[i] = detectors[i].type();
                        fields = types[i].builder(nf.create(headers.get(i))).apply(fields);
                    }

                    return fields.endRecord();
                }));
    }

    private CompletionStage<List<String>> headers() {
        if (this.headers == null) {
            return stream().runWith(Sink.head(), materializer);
        } else {
            return CompletableFuture.completedFuture(headers);
        }
    }

    private Source<List<String>, CompletionStage<IOResult>> stream() {
        Flow<ByteString, Collection<ByteString>, NotUsed> scanner =
            CsvParsing.lineScanner((byte) fieldSeparator, (byte) quoteChar, (byte) escapeChar);

        Function<Collection<ByteString>, List<String>> stringify =
            bytes -> bytes.stream().map(ByteString::utf8String).collect(Collectors.toList());

        return FileIO
            .fromPath(file)
            .via(scanner)
            .map(stringify);
    }

}
