package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.client.converters.api.WriteSummary;
import ada.vcs.client.core.FileSystemDependent;
import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.Repository;
import akka.stream.javadsl.Sink;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface Remote
    extends Comparable<Remote>, Writable, Repository {

    ResourceName alias();

    String info();

    RemoteMemento memento();

    Remote withAlias(ResourceName alias);

    @Override
    default int compareTo(Remote o) {
        return this.alias().getValue().compareTo(o.alias().getValue());
    }

}
