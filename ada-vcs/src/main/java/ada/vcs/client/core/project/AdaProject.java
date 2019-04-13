package ada.vcs.client.core.project;

import ada.vcs.client.core.Dataset;
import ada.vcs.client.core.Target;
import ada.vcs.client.core.remotes.Remote;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface AdaProject {

    void addDataset(Dataset ds);

    void addRemote(Remote remote);

    void addTarget(String dataset, Target target);

    Dataset getDataset(String name);

    Stream<Dataset> getDatasets();

    Remote getRemote(String name);

    Stream<Remote> getRemotes();

    Path getPath();

    Stream<Target> getTargets(String dataset);

    Target getTarget(String dataset, String target);

    void removeDataset(String name);

    void removeRemote(String name);

    void removeTarget(String dataset, String name);

    void updateUpstream(String alias);

    Optional<Remote> getUpstream();

}
