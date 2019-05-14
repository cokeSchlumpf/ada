package ada.vcs.adapters.cli.core.dataset;

import ada.vcs.adapters.cli.core.remotes.Remote;
import ada.vcs.adapters.cli.core.remotes.RemotesFactory;
import ada.vcs.domain.legacy.repository.api.version.VersionDetails;
import ada.vcs.domain.legacy.repository.api.version.VersionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor(staticName = "apply")
public final class RemoteSourceFactory {

    private final ObjectMapper om;

    private final VersionFactory versionFactory;

    private final RemotesFactory remotesFactory;

    public RemoteSource apply(VersionDetails details, Remote remote) {
        return RemoteSource.apply(om, details, remote);
    }

    public RemoteSource apply(InputStream is) throws IOException {
        RemoteSourceMemento memento = om.readValue(is, RemoteSourceMemento.class);
        return apply(memento);
    }

    public RemoteSource apply(RemoteSourceMemento memento) {
        return apply(
            versionFactory.createDetails(memento.getDetails()),
            remotesFactory.createRemote(memento.getRemote()));
    }

}
