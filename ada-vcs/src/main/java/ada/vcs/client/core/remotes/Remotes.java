package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.stream.Stream;

public interface Remotes {

    Optional<Remote> getRemote(String alias);

    Optional<Remote> getRemote(ResourceName alias);

    Stream<Remote> getRemotes();

    Optional<Remote> getUpstream();

    Remotes add(Remote remote);

    Remotes remove(Remote remote);

    Remotes remove(String alias);

    Remotes setUpstream(String alias);

    void writeTo(OutputStream os) throws IOException;

}
