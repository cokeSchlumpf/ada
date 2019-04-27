package ada.vcs.client.core.project;

import ada.vcs.client.core.configuration.AdaConfiguration;
import ada.vcs.client.core.dataset.Dataset;
import ada.vcs.client.core.dataset.RemoteSource;
import ada.vcs.client.core.dataset.Target;
import ada.vcs.client.core.remotes.Remote;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface AdaProject {

    void addDataset(Dataset ds);

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
