package ada.vcs.server.adapters.directives;

import ada.vcs.shared.repository.api.version.VersionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class ServerDirectivesFactory {

    private final VersionFactory versionFactory;

    private final ObjectMapper om;

    public ServerDirectives create(Path repositoryRoot) {
        return ServerDirectivesImpl.apply(versionFactory, om);
    }

}
