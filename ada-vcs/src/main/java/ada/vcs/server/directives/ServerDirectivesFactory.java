package ada.vcs.server.directives;

import ada.vcs.client.core.repository.api.version.VersionFactory;
import ada.vcs.client.core.repository.fs.FileSystemRepositoryFactory;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class ServerDirectivesFactory {

    private final VersionFactory versionFactory;

    private final FileSystemRepositoryFactory repositoryFactory;

    public ServerDirectives create(Path repositoryRoot) {
        return ServerDirectivesImpl.apply(repositoryRoot, versionFactory, repositoryFactory);
    }

}
