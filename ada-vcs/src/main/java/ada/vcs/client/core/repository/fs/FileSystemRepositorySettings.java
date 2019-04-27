package ada.vcs.client.core.repository.fs;

import ada.commons.util.FileSize;
import ada.vcs.client.core.Writable;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

@Value
@AllArgsConstructor(staticName = "apply")
public class FileSystemRepositorySettings implements Writable {

    private final Path root;

    private final ObjectMapper om;

    private final int batchSize;

    private final String detailsFileName;

    private final FileSize maxFileSize;

    private final String recordsFileNameTemplate;

    public static Builder builder(Path root, ObjectMapper om) {
        return Builder.apply(root, om);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, FileSystemRepositorySettingsMemento.apply(batchSize, detailsFileName, maxFileSize, recordsFileNameTemplate));
    }

    @Wither
    @AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "apply")
    public static class Builder {

        private final Path root;

        private final ObjectMapper om;

        private final int batchSize;

        private final String detailsFileName;

        private final FileSize maxFileSize;

        private final String recordsFileNameTemplate;

        public static Builder apply(Path root, ObjectMapper om) {
            return Builder.apply(
                root,
                om,
                128,
                "details.json",
                FileSize.apply(2, FileSize.Unit.GIGABYTES),
                "records-%03d.avro");
        }

        public FileSystemRepositorySettings build() {
            return FileSystemRepositorySettings.apply(
                root, om, batchSize, detailsFileName, maxFileSize, recordsFileNameTemplate);
        }

    }

}
