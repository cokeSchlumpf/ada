package ada.domain.legacy.repository.fs;

import ada.commons.util.FileSize;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileSystemRepositorySettingsMemento {

    private static final String BATCH_SIZE = "batch-size";
    private static final String DETAILS_FILENAME = "details-filename";
    private static final String MAX_FILE_SIZE = "max-file-size";
    private static final String RECORDS_FILENAME_TEMPLATE = "records-filename-template";

    @JsonProperty(BATCH_SIZE)
    private final int batchSize;

    @JsonProperty(DETAILS_FILENAME)
    private final String detailsFilename;

    @JsonProperty(MAX_FILE_SIZE)
    private final FileSize maxFileSize;

    @JsonProperty(RECORDS_FILENAME_TEMPLATE)
    private final String recordsFilenameTemplate;

    @JsonCreator
    public static FileSystemRepositorySettingsMemento apply(
        @JsonProperty(BATCH_SIZE) int batchSize,
        @JsonProperty(DETAILS_FILENAME) String detailsFilename,
        @JsonProperty(MAX_FILE_SIZE) FileSize maxFileSize,
        @JsonProperty(RECORDS_FILENAME_TEMPLATE) String recordsFilenameTemplate) {

        return new FileSystemRepositorySettingsMemento(batchSize, detailsFilename, maxFileSize, recordsFilenameTemplate);
    }

}
