package ada.vcs.client.converters.csv;

import ada.commons.util.Either;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.internal.api.DataSink;
import ada.vcs.client.converters.internal.api.WriteSummary;
import ada.vcs.client.core.FileSystemDependent;
import ada.vcs.client.datatypes.BooleanFormat;
import akka.NotUsed;
import akka.stream.alpakka.csv.javadsl.CsvFormatting;
import akka.stream.alpakka.csv.javadsl.CsvQuotingStyle;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Value
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CSVSink implements DataSink, FileSystemDependent<CSVSink> {

    private static final String OUTPUT = "output";
    private static final String FIELD_SEPARATOR = "field-separator";
    private static final String QUOTE_CHAR = "quote-char";
    private static final String ESCAPE_CHAR = "escape-char";
    private static final String END_OF_LINE = "eol";
    private static final String NULL_VALUE = "null-value";
    private static final String NUMBER_FORMAT = "number-format";
    private static final String BOOLEAN_FORMAT = "boolean-format";

    @JsonProperty(OUTPUT)
    private final Either<Path, PrintStream> output;

    @JsonProperty(FIELD_SEPARATOR)
    private final char fieldSeparator;

    @JsonProperty(QUOTE_CHAR)
    private final char quoteChar;

    @JsonProperty(ESCAPE_CHAR)
    private final char escapeChar;

    @JsonProperty(END_OF_LINE)
    private final String endOfLine;

    @JsonProperty(NULL_VALUE)
    private final String nullValue;

    @JsonProperty(NUMBER_FORMAT)
    private final String numberFormat;

    @JsonProperty(BOOLEAN_FORMAT)
    private final BooleanFormat booleanFormat;

    public static CSVSink apply(Either<Path, PrintStream> output) {
        return CSVSink.apply(output, ';', '"', '\\', "\r\n", "", "#,##0.0000", BooleanFormat.apply("true", "false"));
    }

    public static CSVSink apply(PrintStream out) {
        return CSVSink.apply(Either.right(out));
    }

    public static CSVSink apply(CommandLineConsole out) {
        return CSVSink.apply(Either.right(out.printStream()));
    }

    public static CSVSink apply(Path out) {
        return CSVSink.apply(Either.left(out));
    }

    @JsonCreator
    public static CSVSink apply(
        @JsonProperty(OUTPUT) Either<Path, PrintStream> output,
        @JsonProperty(FIELD_SEPARATOR) char fieldSeparator,
        @JsonProperty(QUOTE_CHAR) char quoteChar,
        @JsonProperty(ESCAPE_CHAR) char escapeChar,
        @JsonProperty(END_OF_LINE) String endOfLine,
        @JsonProperty(NULL_VALUE) String nullValue,
        @JsonProperty(NUMBER_FORMAT) String numberFormat,
        @JsonProperty(BOOLEAN_FORMAT) BooleanFormat booleanFormat) {

        return new CSVSink(output, fieldSeparator, quoteChar, escapeChar, endOfLine, nullValue, numberFormat, booleanFormat);
    }

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema) {
        final AtomicLong count = new AtomicLong();
        final DecimalFormat df = new DecimalFormat(numberFormat);

        Flow<GenericRecord, ByteString, NotUsed> flow = Flow
            .of(GenericRecord.class)
            .map(record -> (Collection<String>) schema
                .getFields()
                .stream()
                .map(field -> {
                    Object value = record.get(field.name());

                    if (value == null) {
                        return nullValue;
                    } else if (value instanceof Double) {
                        return df.format(value);
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value) ? booleanFormat.getTrue() : booleanFormat.getFalse();
                    } else {
                        return value.toString();
                    }
                })
                .collect(Collectors.toList()))
            .map(record -> {
                count.incrementAndGet();
                return record;
            })
            .via(CsvFormatting.format(
                fieldSeparator,
                quoteChar,
                escapeChar,
                endOfLine,
                CsvQuotingStyle.REQUIRED,
                StandardCharsets.UTF_8,
                Optional.empty()));

        return output.map(
            path -> flow
                .toMat(
                    FileIO.toFile(path.toFile()),
                    (i, done) -> done.thenApply(i2 -> WriteSummary.apply(count.get()))),
            out ->
                flow
                    .map(ByteString::utf8String)
                    .toMat(
                        Sink.foreach(out::print),
                        (i, done) -> done.thenApply(i2 -> WriteSummary.apply(count.get()))));
    }

    @Override
    public String getInfo() {
        return output.map(
            path -> String.format("csv(%s)", path),
            os -> "csv(System.out)");
    }

    @Override
    public CSVSink resolve(Path to) {
        return apply(
            output.mapLeft(to::resolve), fieldSeparator, quoteChar,
            escapeChar, endOfLine, nullValue, numberFormat, booleanFormat);
    }

    @Override
    public CSVSink relativize(Path to) {
        return apply(
            output.mapLeft(to::relativize), fieldSeparator, quoteChar,
            escapeChar, endOfLine, nullValue, numberFormat, booleanFormat);
    }

}
