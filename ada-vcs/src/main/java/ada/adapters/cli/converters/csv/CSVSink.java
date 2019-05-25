package ada.adapters.cli.converters.csv;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.commons.util.Either;
import ada.adapters.cli.converters.api.DataSink;
import ada.adapters.cli.converters.api.DataSinkMemento;
import ada.adapters.cli.converters.api.WriteSummary;
import ada.adapters.cli.datatypes.BooleanFormat;
import akka.NotUsed;
import akka.stream.alpakka.csv.javadsl.CsvFormatting;
import akka.stream.alpakka.csv.javadsl.CsvQuotingStyle;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
@Wither
@AllArgsConstructor(staticName = "apply")
public final class CSVSink implements DataSink {

    private final Either<Path, PrintStream> output;

    private final char fieldSeparator;

    private final char quoteChar;

    private final char escapeChar;

    private final String endOfLine;

    private final String nullValue;

    private final String numberFormat;

    private final BooleanFormat booleanFormat;

    public static CSVSink apply(CSVSinkMemento memento) {
        return apply(
            memento.getOutput(), memento.getFieldSeparator(), memento.getQuoteChar(),
            memento.getEscapeChar(), memento.getEndOfLine(), memento.getNullValue(),
            memento.getNumberFormat(), memento.getBooleanFormat());
    }

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

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> sink(Schema schema) {
        final AtomicLong count = new AtomicLong();
        final DecimalFormat df = new DecimalFormat(numberFormat);

        final Function<GenericRecord, Collection<String>> transform = (record) -> {
            count.incrementAndGet();

            return (Collection<String>) schema
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
                .collect(Collectors.toList());
        };

        Flow<GenericRecord, ByteString, NotUsed> flow = Flow
            .of(GenericRecord.class)
            .map(transform::apply)
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
                    FileIO.toPath(path),
                    (i, done) -> done.thenApply(i2 -> WriteSummary.apply(count.get()))),
            out ->
                flow
                    .map(ByteString::utf8String)
                    .toMat(
                        Sink.foreach(out::print),
                        (i, done) -> done.thenApply(i2 -> WriteSummary.apply(count.get()))));
    }

    @Override
    public String info() {
        return output.map(
            path -> String.format("csv(%s)", path),
            os -> "csv(System.out)");
    }

    @Override
    public DataSinkMemento memento() {
        return CSVSinkMemento.apply(
            output, fieldSeparator, quoteChar, escapeChar,
            endOfLine, nullValue, numberFormat, booleanFormat);
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
