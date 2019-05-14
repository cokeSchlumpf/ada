package ada.vcs.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.domain.shared.repository.api.RefSpec;
import ada.vcs.domain.shared.repository.api.Repository;
import ada.vcs.domain.shared.repository.api.User;
import ada.vcs.domain.shared.repository.api.version.Tag;
import ada.vcs.domain.shared.repository.api.version.VersionDetails;
import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
final class FileSystemRemote implements Remote {

    private final ObjectMapper om;

    private final ResourceName alias;

    private final Path dir;

    private final Repository delegate;

    @Override
    public ResourceName alias() {
        return alias;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Remote) {
            return memento().equals(((Remote) obj).memento());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return memento().hashCode();
    }

    @Override
    public String info() {
        return dir.toString();
    }

    @Override
    public RemoteMemento memento() {
        return FileSystemRemoteMemento.apply(alias, dir);
    }

    @Override
    public Remote withAlias(ResourceName alias) {
        return apply(om, alias, dir, delegate);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, FileSystemRemoteMemento.apply(alias, dir));
    }

    @Override
    public CompletionStage<RefSpec.TagRef> tag(User user, RefSpec.VersionRef ref, ResourceName name) {
        return delegate.tag(user, ref, name);
    }

    @Override
    public Source<Tag, NotUsed> tags() {
        return delegate.tags();
    }

    @Override
    public Source<VersionDetails, NotUsed> datasets() {
        return delegate.datasets();
    }

    @Override
    public Sink<GenericRecord, CompletionStage<VersionDetails>> push(VersionDetails details) {
        return delegate.push(details);
    }

    @Override
    public Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec) {
        return delegate.pull(refSpec);
    }

}
