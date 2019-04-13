package ada.vcs.client.core.project;

import ada.vcs.client.core.Dataset;
import ada.vcs.client.core.Target;
import ada.vcs.client.core.remotes.Remote;
import ada.vcs.client.core.remotes.Remotes;
import ada.vcs.client.exceptions.*;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
final class AdaProjectImpl implements AdaProject {

    private final AdaProjectDAO dao;

    @Override
    public void addDataset(Dataset dataset) {
        String name = dataset.getAlias().getValue();

        dao
            .readDataset(name)
            .ifPresent(existing -> {
                throw DatasetAlreadyExistsException.apply(name);
            });

        dao.saveDataset(dataset);
    }

    @Override
    public void addRemote(Remote remote) {
        String name = remote.getAlias().getValue();
        Remotes remotes = dao.readRemotes();

        remotes
            .getRemote(remote.getAlias().getValue())
            .ifPresent(existing -> {
                throw RemoteAlreadyExistsException.apply(name);
            });

        dao.saveRemotes(remotes.add(remote));
    }

    @Override
    public void addTarget(String dataset, Target target) {
        Dataset existing = getDataset(dataset);

        Map<String, Target> targets$next = Maps.newHashMap();
        existing.getTargets().forEach(t -> targets$next.put(t.getAlias().getValue(), t));
        targets$next.put(target.getAlias().getValue(), target);
        dao.saveDataset(existing.withTargets(targets$next));
    }

    @Override
    public Dataset getDataset(String name) {
        return dao
            .readDataset(name)
            .orElseThrow(() -> DatasetNotExistingException.apply(name));
    }

    @Override
    public Stream<Dataset> getDatasets() {
        return dao
            .readDatasets();
    }

    @Override
    public Remote getRemote(String name) {
        return dao
            .readRemotes()
            .getRemote(name)
            .orElseThrow(() -> RemoteNotExistingException.apply(name));
    }

    @Override
    public Stream<Remote> getRemotes() {
        return dao
            .readRemotes()
            .getRemotes();
    }

    @Override
    public Path getPath() {
        return dao.getRoot();
    }

    @Override
    public Stream<Target> getTargets(String dataset) {
        Dataset existing = getDataset(dataset);
        return existing.getTargets();
    }

    @Override
    public Target getTarget(String dataset, String target) {
        Dataset existing = getDataset(dataset);
        return existing
            .getTargets()
            .filter(t -> t.getAlias().getValue().equals(target))
            .findFirst()
            .orElseThrow(() -> TargetNotExistingException.apply(dataset, target));
    }

    @Override
    public void removeDataset(String name) {
        dao
            .readDataset(name)
            .ifPresent(ignore -> dao.removeDataset(name));
    }

    @Override
    public void removeRemote(String name) {
        dao.saveRemotes(dao
            .readRemotes()
            .remove(name));
    }

    @Override
    public void removeTarget(String dataset, String name) {
        Dataset existing = getDataset(dataset);
        Map<String, Target> targets$next = Maps.newHashMap();

        existing
            .getTargets()
            .filter(t -> !t.getAlias().getValue().equals(name))
            .forEach(t -> targets$next.put(t.getAlias().getValue(), t));

        dao.saveDataset(existing.withTargets(targets$next));
    }

    @Override
    public void updateUpstream(String alias) {
        dao
            .readRemotes()
            .getRemote(alias)
            .map(ignore -> {
                Remotes remotes = dao
                    .readRemotes()
                    .setUpstream(alias);

                dao.saveRemotes(remotes);

                return ignore;
            })
            .orElseThrow(() -> RemoteNotExistingException.apply(alias));

    }

    @Override
    public Optional<Remote> getUpstream() {
        return dao
            .readRemotes()
            .getUpstream();
    }

}
