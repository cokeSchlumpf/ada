package ada.vcs.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.client.repositories.RepositoriesClientFactory;
import ada.vcs.domain.shared.repository.api.RefSpec;
import ada.vcs.domain.shared.repository.api.User;
import ada.vcs.domain.shared.repository.api.version.Tag;
import ada.vcs.domain.shared.repository.api.version.VersionDetails;
import ada.vcs.domain.shared.repository.api.version.VersionFactory;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.CompletionStage;

@Value
@AllArgsConstructor(staticName = "apply")
final class HttpRemote implements Remote {

    private final ObjectMapper om;

    private final ActorSystem system;

    private final Materializer materializer;

    private final VersionFactory versionFactory;

    private final ResourceName alias;

    private final URL endpoint;

    private final RepositoriesClientFactory clientFactory;

    public static HttpRemote apply(ObjectMapper om, ActorSystem system, Materializer materializer, VersionFactory versionFactory, HttpRemoteMemento memento) {
        // TODO mw:
        return HttpRemote.apply(om, system, materializer, versionFactory, memento.getAlias(), memento.getEndpoint(), null);
    }

    @Override
    public ResourceName alias() {
        return alias;
    }

    @Override
    public String info() {
        return endpoint.toString();
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
    public RemoteMemento memento() {
        return HttpRemoteMemento.apply(alias, endpoint);
    }

    @Override
    public Remote withAlias(ResourceName alias) {
        return apply(om, system, materializer, versionFactory, alias, endpoint, clientFactory);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, HttpRemoteMemento.apply(alias, endpoint));
    }

    @Override
    public CompletionStage<RefSpec.TagRef> tag(User user, RefSpec.VersionRef ref, ResourceName name) {
        return null;
    }

    @Override
    public Source<Tag, NotUsed> tags() {
        return null;
    }

    @Override
    public Source<VersionDetails, NotUsed> datasets() {
        return null;
    }

    @Override
    public Sink<GenericRecord, CompletionStage<VersionDetails>> push(VersionDetails details) {
        return clientFactory
            .createRepository(endpoint)
            .push(details);
    }

    @Override
    public Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec) {
        return clientFactory
            .createRepository(endpoint)
            .pull(refSpec);
    }

}
