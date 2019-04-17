package ada.vcs.client.core.remotes;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.ResourceName;
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

    public static RemotesFactory apply(ObjectMapperFactory omf) {
        return apply(omf.create(true));
    }

    public static RemotesFactory apply() {
        return apply(ObjectMapperFactory.apply());
    }

    public Remote createFileSystemRemote(ResourceName alias, Path dir) {
        return FileSystemRemote.apply(om, alias, dir);
    }

    public  HttpRemote createHttpRemote(ResourceName alias, URL endpoint) {
        return HttpRemote.apply(om, alias, endpoint);
    }

    public Remote createRemote(InputStream is) throws IOException {
        RemoteMemento memento = om.readValue(is, RemoteMemento.class);
        return createRemote(memento);
    }

    public Remote createRemote(RemoteMemento memento) {
        if (memento instanceof FileSystemRemoteMemento) {
            return FileSystemRemote.apply(om, (FileSystemRemoteMemento) memento);
        } else if (memento instanceof HttpRemoteMemento) {
            return HttpRemote.apply(om, (HttpRemoteMemento) memento);
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
