package ada.adapters.cli.core.project;

import ada.adapters.cli.core.configuration.AdaConfiguration;
import ada.adapters.cli.exceptions.*;
import ada.adapters.cli.core.AdaHome;
import ada.adapters.cli.core.dataset.Dataset;
import ada.adapters.cli.core.dataset.RemoteSource;
import ada.adapters.cli.core.dataset.Target;
import ada.adapters.cli.core.remotes.Remote;
import ada.adapters.cli.core.remotes.Remotes;
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

    private final AdaHome home;

    @Override
    public void addDataset(Dataset dataset) {
        String name = dataset.alias().getValue();

        dao
            .readDataset(name)
            .ifPresent(existing -> {
                throw DatasetAlreadyExistsException.apply(name);
            });

        dao.saveDataset(dataset);
    }

    @Override
    public void addGitIgnore(Path ignore, boolean directory, String comment) {
        dao.addGitIgnore(ignore, directory, comment);
    }

    @Override
    public void addGitIgnore(String ignorePattern, String comment) {
        dao.addGitIgnore(ignorePattern, comment);
    }

    @Override
    public void addRemote(Remote remote) {
        String name = remote.alias().getValue();
        Remotes remotes = dao.readRemotes();

        remotes
            .getRemote(remote.alias().getValue())
            .ifPresent(existing -> {
                throw RemoteAlreadyExistsException.apply(name);
            });

        dao.saveRemotes(remotes.add(remote));
    }

    @Override
    public void addTarget(String dataset, Target target) {
        Dataset existing = getDataset(dataset);

        Map<String, Target> targets$next = Maps.newHashMap();
        existing.getTargets().forEach(t -> targets$next.put(t.alias().getValue(), t));
        targets$next.put(target.alias().getValue(), target);
        dao.saveDataset(existing.withTargets(targets$next));
    }

    @Override
    public AdaConfiguration getConfiguration() {
        return AdaProjectConfiguration.apply(dao, home);
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
    public Path path() {
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
            .filter(t -> t.alias().getValue().equals(target))
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
            .filter(t -> !t.alias().getValue().equals(name))
            .forEach(t -> targets$next.put(t.alias().getValue(), t));

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
    public void updateRemoteSource(String dataset, RemoteSource rs) {
        Dataset existing = getDataset(dataset);
        dao.saveDataset(existing.withRemoteSource(rs));
    }

    @Override
    public Optional<Remote> getUpstream() {
        return dao
            .readRemotes()
            .getUpstream();
    }

}
