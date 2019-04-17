package ada.vcs.client.core.dataset;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.DataSink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Target implements Comparable<Target> {

    @JsonProperty("alias")
    private final ResourceName alias;

    @JsonProperty("sink")
    private final DataSink sink;

    @JsonCreator
    public static Target apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("sink") DataSink sink) {

        return new Target(alias, sink);
    }

    @Override
    public int compareTo(Target o) {
        return this.alias.getValue().compareTo(o.alias.getValue());
    }

}
