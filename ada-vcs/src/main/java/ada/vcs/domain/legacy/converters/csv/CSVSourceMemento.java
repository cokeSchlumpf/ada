package ada.vcs.domain.legacy.converters.csv;

import ada.vcs.domain.legacy.converters.api.DataSourceMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CSVSourceMemento implements DataSourceMemento {

    private static final String FILE = "file";
    private static final String FIELD_SEPARATOR = "field-separator";
    private static final String COMMENT_CHAR = "comment-char";
    private static final String QUOTE_CHAR = "quote-char";
    private static final String ESCAPE_CHAR = "escape-char";
    private static final String HEADERS = "headers";
    private static final String RECORDS_ANALYZED = "records-analyzed";

    @JsonProperty(FILE)
    private final Path file;

    @JsonProperty(FIELD_SEPARATOR)
    private final Character fieldSeparator;

    @JsonProperty(COMMENT_CHAR)
    private final Character commentChar;

    @JsonProperty(QUOTE_CHAR)
    private final Character quoteChar;

    @JsonProperty(ESCAPE_CHAR)
    private final Character escapeChar;

    @JsonProperty(HEADERS)
    private final List<String> headers;

    @JsonProperty(RECORDS_ANALYZED)
    private final Integer recordsAnalyzed;

    @JsonCreator
    public static CSVSourceMemento apply(
        @JsonProperty(FILE) Path file,
        @Nullable @JsonProperty(FIELD_SEPARATOR) Character fieldSeparator,
        @Nullable @JsonProperty(COMMENT_CHAR) Character commentChar,
        @Nullable @JsonProperty(QUOTE_CHAR) Character quoteChar,
        @Nullable @JsonProperty(ESCAPE_CHAR) Character escapeChar,
        @Nullable @JsonProperty(HEADERS) List<String> headers,
        @Nullable @JsonProperty(RECORDS_ANALYZED) Integer recordsAnalyzed) {

        return new CSVSourceMemento(file, fieldSeparator, commentChar, quoteChar, escapeChar, headers, recordsAnalyzed);
    }

}
