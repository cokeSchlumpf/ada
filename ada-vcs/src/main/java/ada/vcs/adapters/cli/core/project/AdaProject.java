package ada.vcs.adapters.cli.core.project;

import ada.vcs.adapters.cli.core.configuration.AdaConfiguration;
import ada.vcs.adapters.cli.core.dataset.Dataset;
import ada.vcs.adapters.cli.core.dataset.RemoteSource;
import ada.vcs.adapters.cli.core.dataset.Target;
import ada.vcs.adapters.cli.core.remotes.Remote;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface AdaProject {

    void addDataset(Dataset ds);

    void addGitIgnore(Path ignore, boolean directory, String comment);

    void addGitIgnore(String ignorePattern, String comment);

    void addRemote(Remote remote);

    void addTarget(String dataset, Target target);

    AdaConfiguration getConfiguration();

    Dataset getDataset(String name);

    Stream<Dataset> getDatasets();

    Remote getRemote(String name);

    Stream<Remote> getRemotes();

    Path path();

    Stream<Target> getTargets(String dataset);

    Target getTarget(String dataset, String target);

    void removeDataset(String name);

    void removeRemote(String name);

    void removeTarget(String dataset, String name);

    void updateUpstream(String alias);

    void updateRemoteSource(String dataset, RemoteSource rs);

    Optional<Remote> getUpstream();

}
