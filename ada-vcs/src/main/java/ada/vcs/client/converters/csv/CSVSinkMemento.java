package ada.vcs.client.converters.csv;

import ada.commons.util.Either;
import ada.vcs.client.converters.api.DataSinkMemento;
import ada.vcs.client.datatypes.BooleanFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.PrintStream;
import java.nio.file.Path;

@Value
@AllArgsConstructor(staticName = "apply")
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

}
