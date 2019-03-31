package ada.vcs.client.converters.csv;

import ada.commons.NameFactory;
import ada.vcs.client.converters.internal.api.DataSource;
import ada.vcs.client.converters.internal.api.ReadableDataSource;
import ada.vcs.client.converters.internal.contexts.FileContext;
import ada.vcs.client.datatypes.DataTypeMatcher;
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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CSVSource implements DataSource<FileContext> {

    private static final String FILE = "file";
    private static final String FIELD_SEPARATOR = "field-separator";
    private static final String COMMENT_CHAR = "comment-char";
    private static final String QUOTE_CHAR = "quote-char";
    private static final String ESCAPE_CHAR = "escape-char";
    private static final String HEADERS = "headers";
    private static final String SCHEMA = "schema";
    private static final String RECORDS_ANALYZED = "records-analyzed";

    @JsonProperty(FILE)
    private final Path file;

    @JsonProperty(FIELD_SEPARATOR)
    private final char fieldSeparator;

    @JsonProperty(COMMENT_CHAR)
    private final char commentChar;

    @JsonProperty(QUOTE_CHAR)
    private final char quoteChar;

    @JsonProperty(ESCAPE_CHAR)
    private final char escapeChar;

    @JsonProperty(HEADERS)
    private final List<String> headers;

    @JsonProperty(SCHEMA)
    private final Schema schema;

    @JsonProperty(RECORDS_ANALYZED)
    private final int recordsAnalyzed;

    @JsonCreator
    public static CSVSource apply(
        @JsonProperty(FILE) Path file,
        @JsonProperty(FIELD_SEPARATOR) Character fieldSeparator,
        @JsonProperty(COMMENT_CHAR) Character commentChar,
        @JsonProperty(QUOTE_CHAR) Character quoteChar,
        @JsonProperty(ESCAPE_CHAR) Character escapeChar,
        @JsonProperty(HEADERS) List<String> headers,
        @JsonProperty(SCHEMA) Schema schema,
        @JsonProperty(RECORDS_ANALYZED) Integer recordsAnalyzed) {

        CSVSourceBuilder b = new CSVSourceBuilder(file);

        if (fieldSeparator != null) b.fieldSeparator(fieldSeparator);
        if (commentChar != null) b.commentChar(commentChar);
        if (quoteChar != null) b.quoteChar(quoteChar);
        if (escapeChar != null) b.escapeChar(escapeChar);
        if (headers != null && headers.size() > 0) b.headers(headers);
        if (schema != null) b.schema(schema);
        if (recordsAnalyzed != null) b.recordsAnalyzed(recordsAnalyzed);

        return b.build();
    }

    public static CSVSourceBuilder builder(Path file) {
        return new CSVSourceBuilder(file);
    }

    @Override
    public CompletionStage<ReadableDataSource<FileContext>> analyze(Materializer materializer) {
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

                    if (this.schema != null) {
                        return ReadableCSVSource.apply(this, schema, matchers, offset);
                    } else {
                        SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder
                            .record(nf.create(FilenameUtils.removeExtension(file.getFileName().toString())))
                            .namespace("ada.autodetect")
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

    private CompletionStage<List<String>> headers(Materializer materializer) {
        return read().runWith(Sink.head(), materializer);
    }

    public Optional<Schema> getSchema() {
        return Optional.ofNullable(schema);
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

    public final static class CSVSourceBuilder {

        private final Path file;

        private char fieldSeparator = ';';

        private char commentChar = '#';

        private char quoteChar = '\"';

        private char escapeChar = '\\';

        private int recordsAnalyzed = 100;

        private List<String> headers = Lists.newArrayList();

        private Schema schema = null;

        private CSVSourceBuilder(Path file) {
            this.file = file;
        }

        public CSVSource build() {
            return new CSVSource(
                file, fieldSeparator, commentChar, quoteChar, escapeChar, headers, schema, recordsAnalyzed);
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

        public CSVSourceBuilder schema(Schema schema) {
            this.schema = schema;
            return this;
        }

    }

}
