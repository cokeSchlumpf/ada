package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.internal.api.WriteSummary;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.net.URL;
import java.util.concurrent.CompletionStage;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRemote implements Remote {

    private final ResourceName alias;

    private final URL endpoint;

    @JsonCreator
    public static HttpRemote apply(
        @JsonProperty("alias") ResourceName alias,
        @JsonProperty("endpoint") URL endpoint) {

        return new HttpRemote(alias, endpoint);
    }

    @Override
    public String getInfo() {
        return endpoint.toString();
    }

    @Override
    public Sink<GenericRecord, CompletionStage<WriteSummary>> push(Schema schema) {
        return null;
    }

}
