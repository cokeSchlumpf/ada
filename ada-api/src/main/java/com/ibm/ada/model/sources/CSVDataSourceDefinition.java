package com.ibm.ada.model.sources;

import com.ibm.ada.model.RelativePath;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Optional;

@Value
@AllArgsConstructor(staticName = "apply")
public class CSVDataSourceDefinition implements DataSourceDefinition {

    /**
     * The source file relative to the project directory.
     */
    private final RelativePath path;

    /**
     * The row number of the header row, if it is <= 0 no header row is expected.
     */
    private final int headerRow;

    /**
     * The column separator of the CSV.
     */
    private final String columnSeparator;

    /**
     * The charset of the CSV file.
     */
    private final Charset charset;

    /**
     * A string which determines that a line should be ignored as comment.
     */
    @Nullable
    private final String commentLineIndicator;

    public Optional<String> getCommentLineIndicator() {
        return Optional.ofNullable(commentLineIndicator);
    }

}
