package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.WriteSummary;
import ada.vcs.client.core.FileSystemDependent;
import akka.stream.javadsl.Sink;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletionStage;

public interface Remote extends Comparable<Remote>, FileSystemDependent<Remote> {

    ResourceName alias();

    String info();

    RemoteMemento memento();

    Sink<GenericRecord, CompletionStage<WriteSummary>> push(Schema schema);

    void writeTo(OutputStream os) throws IOException;

    @Override
    default int compareTo(Remote o) {
        return this.alias().getValue().compareTo(o.alias().getValue());
    }

}
