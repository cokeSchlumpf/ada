package ada.adapters.cli.core.remotes;

import ada.commons.util.ResourceName;
import ada.commons.io.Writable;

import java.util.Optional;
import java.util.stream.Stream;

public interface Remotes extends Writable {

    Optional<Remote> getRemote(String alias);

    Optional<Remote> getRemote(ResourceName alias);

    Stream<Remote> getRemotes();

    Optional<Remote> getUpstream();

    Remotes add(Remote remote);

    Remotes remove(Remote remote);

    Remotes remove(String alias);

    Remotes setUpstream(String alias);

}
