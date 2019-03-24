package ada.vcs.client.converters;

import ada.commons.NameFactory;
import ada.vcs.client.datatypes.DataType;
import ada.vcs.client.datatypes.DataTypeDetector;
import akka.NotUsed;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.alpakka.csv.javadsl.CsvParsing;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(staticName = "apply")
public class CSVFile {

    private final Path file;

    private final char fieldSeparator;

    private final char commentChar;

    private final char quoteChar;

    private final char escapeChar;

    private final List<String> headers;

    public Source<List<String>, CompletionStage<IOResult>> read() {
        Flow<ByteString, Collection<ByteString>, NotUsed> scanner =
            CsvParsing.lineScanner((byte) fieldSeparator, (byte) quoteChar, (byte) escapeChar);

        Function<Collection<ByteString>, List<String>> stringify =
            bytes -> bytes.stream().map(ByteString::utf8String).collect(Collectors.toList());

        String comment = Character.toString(commentChar);

        return FileIO
            .fromPath(file)
            .via(scanner)
            .map(stringify)
            .filter(l -> !l.isEmpty())
            .filter(l -> !l.get(0).startsWith(comment));
    }

    public Headers headers() {
        return Headers.apply();
    }

    public RecordSource records() {
        return null;
    }

    @AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
    public final class Headers {

        public Source<String, NotUsed> source() {
            if (CSVFile.this.headers == null) {
                return read()
                    .take(1)
                    .mapConcat(l -> l)
                    .mapMaterializedValue(i -> NotUsed.getInstance());
            } else {
                return Source.from(CSVFile.this.headers);
            }
        }

        public CompletionStage<List<String>> list(Materializer materializer) {
            return read().runWith(Sink.head(), materializer);
        }

    }

    @AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
    public static final class Records implements RecordSource {

        private final CSVFile file;

        private final CompletionStage<Pair<DataType[], Schema>> schema;

        public static Records apply(CSVFile file, Materializer materializer, int analyzeRecordsCount) {
            CompletionStage<Pair<DataType[], Schema>> schema = file.headers()
                .list(materializer)
                .thenCompose(headers -> file.read()
                    .drop(1) // ignore header
                    .take(analyzeRecordsCount)
                    .grouped(analyzeRecordsCount)
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
                            .record(nf.create(FilenameUtils.removeExtension(file.file.getFileName().toString())))
                            .namespace("ada.autodetect")
                            .fields();

                        for (int i = 0; i < headers.size(); i++) {
                            types[i] = detectors[i].type();
                            fields = types[i].builder(nf.create(headers.get(i))).apply(fields);
                        }

                        return Pair.apply(types, fields.endRecord());
                    }));

            return apply(file, schema);
        }

        @Override
        public CompletionStage<Schema> schema(int analyzeRecordsCount) {
            return schema.thenApply(Pair::second);
        }

        @Override
        public Source<GenericRecord, NotUsed> source() {
            return Source
                .fromCompletionStage(schema)
                .flatMapConcat(pair -> file
                    .read()
                    .drop(file.headers == null || file.headers.isEmpty() ? 0 : 1)
                    .map(values -> {
                        GenericRecordBuilder record = new GenericRecordBuilder(pair.second());
                        Schema schema = pair.second();

                        for (int i = 0; i < schema.getFields().size() && i < values.size(); i++) {
                            Schema.Field field = schema.getFields().get(i);
                            record.set(field, pair.first()[i].parse(values.get(i)));
                        }

                        return record.build();
                    }));
        }

    }

}
