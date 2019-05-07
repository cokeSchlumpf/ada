package ada.vcs.server.adapters.directives;

import ada.vcs.client.core.repository.api.version.VersionFactory;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class ServerDirectivesFactory {

    private final VersionFactory versionFactory;

    public ServerDirectives create(Path repositoryRoot) {
        return ServerDirectivesImpl.apply(versionFactory);
    }

}
