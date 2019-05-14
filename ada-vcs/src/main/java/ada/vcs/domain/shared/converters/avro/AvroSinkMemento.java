package ada.vcs.domain.shared.converters.avro;

import ada.vcs.domain.shared.converters.api.DataSinkMemento;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AvroSinkMemento implements DataSinkMemento {

    @JsonProperty("path")
    private final Path path;

    @JsonCreator
    public static AvroSinkMemento apply(@JsonProperty("path") Path path) {
        return new AvroSinkMemento(path);
    }

}
