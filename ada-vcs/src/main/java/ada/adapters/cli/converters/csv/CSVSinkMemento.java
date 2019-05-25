package ada.adapters.cli.converters.csv;

import ada.commons.util.Either;
import ada.adapters.cli.converters.api.DataSinkMemento;
import ada.adapters.cli.datatypes.BooleanFormat;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.PrintStream;
import java.nio.file.Path;

@Value
public final class CSVSinkMemento implements DataSinkMemento {

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

    @JsonCreator
    private CSVSinkMemento(
        @JsonProperty(OUTPUT) Either<Path, PrintStream> output,
        @JsonProperty(FIELD_SEPARATOR) char fieldSeparator,
        @JsonProperty(QUOTE_CHAR) char quoteChar,
        @JsonProperty(ESCAPE_CHAR) char escapeChar,
        @JsonProperty(END_OF_LINE) String endOfLine,
        @JsonProperty(NULL_VALUE) String nullValue,
        @JsonProperty(NUMBER_FORMAT) String numberFormat,
        @JsonProperty(BOOLEAN_FORMAT) BooleanFormat booleanFormat) {

        this.output = output;
        this.fieldSeparator = fieldSeparator;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
        this.endOfLine = endOfLine;
        this.nullValue = nullValue;
        this.numberFormat = numberFormat;
        this.booleanFormat = booleanFormat;
    }

    public static CSVSinkMemento apply(Either<Path, PrintStream> output, char fieldSeparator, char quoteChar, char escapeChar, String endOfLine, String nullValue, String numberFormat, BooleanFormat booleanFormat) {
        return new CSVSinkMemento(output, fieldSeparator, quoteChar, escapeChar, endOfLine, nullValue, numberFormat, booleanFormat);
    }
}
