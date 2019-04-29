package ada.vcs.client.converters.csv;

import ada.commons.util.NameFactory;
import ada.commons.util.Operators;
import ada.vcs.client.converters.api.DataSource;
import ada.vcs.client.converters.api.DataSourceMemento;
import ada.vcs.client.converters.api.ReadableDataSource;
import ada.vcs.client.datatypes.DataTypeMatcher;
import akka.NotUsed;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.alpakka.csv.javadsl.CsvParsing;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CSVSource implements DataSource {

    private final Path file;

    private final char fieldSeparator;

    private final char commentChar;

    private final char quoteChar;

    private final char escapeChar;

    private final List<String> headers;

    private final int recordsAnalyzed;

    public static CSVSource apply(
        Path file, Character fieldSeparator, Character commentChar,
        Character quoteChar, Character escapeChar, List<String> headers, Integer recordsAnalyzed) {

        CSVSourceBuilder b = new CSVSourceBuilder(file);

        if (fieldSeparator != null) b.fieldSeparator(fieldSeparator);
        if (commentChar != null) b.commentChar(commentChar);
        if (quoteChar != null) b.quoteChar(quoteChar);
        if (escapeChar != null) b.escapeChar(escapeChar);
        if (headers != null && headers.size() > 0) b.headers(headers);
        if (recordsAnalyzed != null) b.recordsAnalyzed(recordsAnalyzed);

        return b.build();
    }

    public static CSVSource apply(CSVSourceMemento memento) {
        return apply(
            memento.getFile(), memento.getFieldSeparator(), memento.getCommentChar(),
            memento.getQuoteChar(), memento.getEscapeChar(), memento.getHeaders(), memento.getRecordsAnalyzed());
    }

    public static CSVSourceBuilder builder(Path file) {
        return new CSVSourceBuilder(file);
    }

    @Override
    public CompletionStage<ReadableDataSource> analyze(Materializer materializer, Schema schema) {
        final NameFactory nf = NameFactory.apply(NameFactory.Defaults.LOWERCASE_UNDERSCORED);
        final int offset = headers != null && headers.size() > 0 ? 0 : 1;

        return headers(materializer)
            .thenApply(headers -> headers
                .stream()
                .map(nf::create)
                .collect(Collectors.toList()))
            .thenCompose(headers -> read()
                .drop(offset)
                .grouped(recordsAnalyzed)
                .runWith(Sink.head(), materializer)
                .thenApply(samples -> {
                    Map<String, DataTypeMatcher> matchers = Maps.newHashMap();

                    for (String header : headers) {
                        matchers.put(header, DataTypeMatcher.apply());
                    }

                    for (List<String> sample : samples) {
                        for (int i = 0; i < headers.size(); i++) {
                            if (i > sample.size() - 1) {
                                matchers.get(headers.get(i)).hint(null);
                            } else {
                                matchers.get(headers.get(i)).hint(sample.get(i));
                            }
                        }
                    }

                    if (schema != null) {
                        return ReadableCSVSource.apply(this, schema, matchers, offset);
                    } else {
                        SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder
                            .record(nf.create(FilenameUtils.removeExtension(file.getFileName().toString())))
                            .fields();

                        for (String header : headers) {
                            fields = matchers
                                .get(header)
                                .match()
                                .type(header)
                                .builder()
                                .apply(fields);
                        }

                        return ReadableCSVSource.apply(this, fields.endRecord(), matchers, offset);
                    }
                }));
    }

    @Override
    public CompletionStage<ReadableDataSource> analyze(Materializer materializer) {
        return analyze(materializer, null);
    }

    @Override
    public String info() {
        return String.format("csv(%s)", file.toString());
    }

    @Override
    public DataSourceMemento memento() {
        return CSVSourceMemento.apply(
            file, fieldSeparator, commentChar, quoteChar, escapeChar,
            headers, recordsAnalyzed);
    }

    private CompletionStage<List<String>> headers(Materializer materializer) {
        return read().runWith(Sink.head(), materializer);
    }

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

    @Override
    public CSVSource resolve(Path to) {
        return apply(to.resolve(file), fieldSeparator, commentChar, quoteChar, escapeChar, headers, recordsAnalyzed);
    }

    @Override
    public CSVSource relativize(Path to) {
        return apply(to.relativize(file), fieldSeparator, commentChar, quoteChar, escapeChar, headers, recordsAnalyzed);
    }

    public final static class CSVSourceBuilder {

        private final Path file;

        private char fieldSeparator = ';';

        private char commentChar = '#';

        private char quoteChar = '\"';

        private char escapeChar = '\\';

        private int recordsAnalyzed = 100;

        private List<String> headers = Lists.newArrayList();

        private CSVSourceBuilder(Path file) {
            this.file = file;
        }

        public CSVSource build() {
            return new CSVSource(
                file, fieldSeparator, commentChar, quoteChar, escapeChar, headers, recordsAnalyzed);
        }

        public CSVSourceBuilder commentChar(char commentChar) {
            this.commentChar = commentChar;
            return this;
        }

        public CSVSourceBuilder escapeChar(char escapeChar) {
            this.escapeChar = escapeChar;
            return this;
        }

        public CSVSourceBuilder fieldSeparator(char fieldSeparator) {
            this.fieldSeparator = fieldSeparator;
            return this;
        }

        public CSVSourceBuilder headers(List<String> headers) {
            this.headers = headers;
            return this;
        }

        public CSVSourceBuilder quoteChar(char quoteChar) {
            this.quoteChar = quoteChar;
            return this;
        }

        public CSVSourceBuilder recordsAnalyzed(int recordsAnalyzed) {
            this.recordsAnalyzed = recordsAnalyzed;
            return this;
        }

    }

}
