package ada.vcs.domain.legacy.converters.local;

import ada.vcs.domain.legacy.converters.api.DataSinkMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalSinkMemento implements DataSinkMemento {

    private final Path directory;

    @JsonCreator
    public static LocalSinkMemento apply(
        @JsonProperty("directory") Path directory) {

        return new LocalSinkMemento(directory);
    }

}
