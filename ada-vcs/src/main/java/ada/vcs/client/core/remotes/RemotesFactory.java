package ada.vcs.client.core.remotes;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.Repository;
import ada.vcs.client.core.repository.api.version.VersionFactory;
import ada.vcs.client.core.repository.fs.FileSystemRepositoryFactory;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply")
public final class RemotesFactory {

    private ObjectMapper om;

    private ActorSystem system;

    private VersionFactory versionFactory;

    public static RemotesFactory apply(ObjectMapperFactory omf, ActorSystem system, VersionFactory versionFactory) {
        return apply(omf.create(true), system, versionFactory);
    }

    public Remote createFileSystemRemote(ResourceName alias, Path dir) {
        Repository delegate = FileSystemRepositoryFactory
            .apply(om, ActorMaterializer.create(system), versionFactory)
            .create(dir);

        return FileSystemRemote.apply(om, alias, dir, delegate);
    }

    public  HttpRemote createHttpRemote(ResourceName alias, URL endpoint) {
        return HttpRemote.apply(om, system, alias, endpoint);
    }

    public Remote createRemote(InputStream is) throws IOException {
        RemoteMemento memento = om.readValue(is, RemoteMemento.class);
        return createRemote(memento);
    }

    public Remote createRemote(RemoteMemento memento) {
        if (memento instanceof FileSystemRemoteMemento) {
            FileSystemRemoteMemento m = (FileSystemRemoteMemento) memento;
            return createFileSystemRemote(m.getAlias(), m.getDir());
        } else if (memento instanceof HttpRemoteMemento) {
            return HttpRemote.apply(om, system, (HttpRemoteMemento) memento);
        } else {
            String message = String.format("Unimplemented remote type `%s`.", memento.getClass());
            throw new RuntimeException(message);
        }
    }

    public Remotes createRemotes(InputStream is) throws IOException {
        RemotesMemento memento = om.readValue(is, RemotesMemento.class);
        List<Remote> remotes = memento
            .getRemotes()
            .stream()
            .map(this::createRemote)
            .collect(Collectors.toList());

        return createRemotes(remotes, memento.getUpstream());
    }

    public Remotes createRemotes(List<Remote> remotes, ResourceName upstream) {
        return RemotesImpl.apply(om, remotes, upstream);
    }

    public Remotes createRemotes(List<Remote> remotes) {
        return RemotesImpl.apply(om, remotes);
    }

    public Remotes createRemotes() {
        return RemotesImpl.apply(om);
    }

}
