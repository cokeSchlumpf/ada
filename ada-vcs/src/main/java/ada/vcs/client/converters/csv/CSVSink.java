package ada.vcs.client.converters.csv;

import ada.commons.Either;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.internal.api.DataSink;
import ada.vcs.client.converters.internal.api.WriteSummary;
import akka.NotUsed;
import akka.stream.alpakka.csv.javadsl.CsvFormatting;
import akka.stream.alpakka.csv.javadsl.CsvQuotingStyle;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply")
public final class CSVSink implements DataSink {

    private final Either<Path, PrintStream> output;

    private final char fieldSeparator;

    private final char quoteChar;

    private final char escapeChar;

    private final String endOfLine;

    private final String nullValue;

    private final NumberFormat numberFormat;

    private final Pair<String, String> booleanFormat;

    public static CSVSink apply(Either<Path, PrintStream> output) {
        return CSVSink.apply(output, ';', '"', '\\', "\r\n", "", NumberFormat.getNumberInstance(), Pair.of("true", "false"));
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

        Flow<GenericRecord, ByteString, NotUsed> flow = Flow
            .of(GenericRecord.class)
            .map(record -> (Collection<String>) schema
                .getFields()
                .stream()
                .map(field -> {
                    Object value = record.get(field.name());
                    count.incrementAndGet();

                    if (value == null) {
                        return nullValue;
                    } else if (value instanceof Double) {
                        return numberFormat.format(value);
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value) ? booleanFormat.getLeft() : booleanFormat.getRight();
                    } else {
                        return value.toString();
                    }
                })
                .collect(Collectors.toList()))
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

}
