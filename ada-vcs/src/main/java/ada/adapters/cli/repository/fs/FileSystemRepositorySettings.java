package ada.adapters.cli.repository.fs;

import ada.commons.util.FileSize;
import ada.commons.io.Writable;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.io.IOException;
import java.io.OutputStream;

@Value
@AllArgsConstructor(staticName = "apply")
public class FileSystemRepositorySettings implements Writable {

    private final ObjectMapper om;

    private final int batchSize;

    private final String detailsFileName;

    private final FileSize maxFileSize;

    private final String recordsFileNameTemplate;

    public static FileSystemRepositorySettings apply(ObjectMapper om, FileSystemRepositorySettingsMemento memento) {
        return apply(
            om, memento.getBatchSize(), memento.getDetailsFilename(),
            memento.getMaxFileSize(), memento.getRecordsFilenameTemplate());
    }

    public static Builder builder(ObjectMapper om) {
        return Builder.apply(om);
    }

    public FileSystemRepositorySettingsMemento memento() {
        return FileSystemRepositorySettingsMemento.apply(batchSize, detailsFileName, maxFileSize, recordsFileNameTemplate);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, memento());
    }

    @Wither
    @AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "apply")
    public static class Builder {

        private final ObjectMapper om;

        private final int batchSize;

        private final String detailsFileName;

        private final FileSize maxFileSize;

        private final String recordsFileNameTemplate;

        public static Builder apply(ObjectMapper om) {
            return Builder.apply(
                om,
                128,
                "details.json",
                FileSize.apply(2, FileSize.Unit.GIGABYTES),
                "records-%03d.avro");
        }

        public FileSystemRepositorySettings build() {
            return FileSystemRepositorySettings.apply(om, batchSize, detailsFileName, maxFileSize, recordsFileNameTemplate);
        }

    }

}
