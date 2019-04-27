package ada.vcs.client.core.dataset;

import ada.vcs.client.converters.api.Monitor;
import ada.vcs.client.converters.api.ReadSummary;
import ada.vcs.client.converters.api.ReadableDataSource;
import ada.vcs.client.core.Writable;
import ada.vcs.client.core.remotes.Remote;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class RemoteSource implements ReadableDataSource, Writable {

    private final ObjectMapper om;

    private final VersionDetails details;

    private final Remote remote;

    public RemoteSourceMemento memento() {
        return RemoteSourceMemento.apply(details.memento(), remote.memento());
    }

    @Override
    public Schema schema() {
        return details.schema();
    }

    @Override
    public RefSpec.VersionRef ref() {
        return RefSpec.VersionRef.apply(details.id());
    }

    @Override
    public Source<GenericRecord, CompletionStage<ReadSummary>> getRecords(Monitor monitor) {
        // TODO ReadSummary vs. VersionDetails!?
        return remote
            .pull(ref())
            .mapMaterializedValue(d -> d.thenApply(i -> ReadSummary.apply(0, 0)));
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, memento());
    }

}
