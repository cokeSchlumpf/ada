package ada.vcs.adapters.cli.core.project;

import ada.vcs.adapters.cli.core.AdaHome;
import ada.vcs.adapters.cli.core.configuration.AdaConfigurationFactory;
import ada.vcs.adapters.cli.core.dataset.DatasetFactory;
import ada.vcs.adapters.cli.core.remotes.RemotesFactory;
import lombok.AllArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
public final class AdaProjectFactory {

    private final AdaConfigurationFactory configurationFactory;

    private final RemotesFactory remotesFactory;

    private final DatasetFactory datasetFactory;

    private final AdaHome home;

    public AdaProject init() {
        return init(Paths.get(System.getProperty("user.dir")));
    }

    public AdaProject init(Path where) {
        AdaProjectDAO dao = AdaProjectDAO.apply(configurationFactory, remotesFactory, datasetFactory, where);
        return AdaProjectImpl.apply(dao, home);
    }


    public Optional<AdaProject> from(Path path) {
        if (Files.isDirectory(path) && Files.exists(path.resolve(".ada"))) {
            return Optional.of(init(path));
        } else if (path.getParent() != null) {
            return from(path.getParent());
        } else {
            return Optional.empty();
        }
    }

    public Optional<AdaProject> fromHere() {
        return from(Paths.get(System.getProperty("user.dir")));
    }

}
