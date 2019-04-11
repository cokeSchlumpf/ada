package ada.vcs.client.core;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.client.core.remotes.Remote;
import ada.vcs.client.core.remotes.Remotes;
import ada.vcs.client.exceptions.DatasetAlreadyExistsException;
import ada.vcs.client.exceptions.DatasetNotExistingException;
import ada.vcs.client.exceptions.TargetNotExistingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Value
@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class AdaProject {

    private static final String ADA_DIR = ".ada";

    private static final String LOCAL = "local";

    private static final String DATASETS = "datasets";

    private static final String REMOTES = "remotes.json";

    private final Path path;

    private final ObjectMapperFactory om;

    public static Optional<AdaProject> fromHere() {
        return from(Paths.get(System.getProperty("user.dir")));
    }

    public static Optional<AdaProject> from(Path path) {
        if (Files.isDirectory(path) && Files.exists(path.resolve(ADA_DIR))) {
            return Optional.of(AdaProject.apply(path.resolve(ADA_DIR), ObjectMapperFactory.apply()));
        } else if (path.getParent() != null) {
            return from(path.getParent());
        } else {
            return Optional.empty();
        }
    }

    public static AdaProject init() {
        return init(Paths.get(System.getProperty("user.dir")));
    }

    public static AdaProject init(Path where) {
        try {
            final ObjectMapperFactory omf = ObjectMapperFactory.apply();
            final ObjectMapper om = omf.create(true);
            final Path dir = where.resolve(ADA_DIR);

            if (!Files.exists(dir)) Files.createDirectory(dir);

            final Path gitignore = where.resolve(".gitignore");
            final Path local = where.relativize(dir.resolve(LOCAL));
            final Path remotes = dir.resolve(REMOTES);

            if (!Files.exists(gitignore) || Files.readAllLines(gitignore).stream().noneMatch(s -> s.contains(local.toString()))) {
                String ignore = System.lineSeparator() +
                    "# Ada local cache" +
                    System.lineSeparator() +
                    local + "/**/*" +
                    System.lineSeparator();

                Files.write(gitignore, ignore.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            }

            if (!Files.exists(dir.resolve(DATASETS))) {
                Files.createDirectories(dir.resolve(DATASETS));
            }

            if (!Files.exists(remotes)) {
                om.writeValue(remotes.toFile(), Remotes.apply());
            }

            return AdaProject.apply(dir, omf);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void addDataset(Dataset ds) {
        try {
            Path file = path.resolve(DATASETS).resolve(ds.getAlias().getValue() + ".json");

            if (Files.exists(file)) {
                throw DatasetAlreadyExistsException.apply(ds.getAlias().getValue());
            } else {
                om.create(true).writeValue(file.toFile(), ds);
            }
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void addTarget(String dataset, Target target) {
        Dataset ds = getDatasetOptional(dataset).orElseThrow(() -> DatasetNotExistingException.apply(dataset));

        Map<String, Target> targets$next = Maps.newHashMap();
        ds.getTargets().forEach(t -> targets$next.put(t.getAlias().getValue(), t));
        targets$next.put(target.getAlias().getValue(), target);

        updateDataset(Dataset.apply(ds.getAlias(), ds.getSource(), ds.getSchema(), targets$next));
    }

    public Stream<Target> getTargets(String dataset) {
        Dataset ds = getDatasetOptional(dataset).orElseThrow(() -> DatasetNotExistingException.apply(dataset));
        return ds.getTargets();
    }

    public Target getTarget(String dataset, String target) {
        return getTargets(dataset)
            .filter(t -> t.getAlias().getValue().equals(target))
            .findFirst()
            .orElseThrow(() -> TargetNotExistingException.apply(dataset, target));
    }

    public void addRemote(Remote remote) {
        Remotes remotes = getRemotes$internal().add(remote);
        updateRemotes(remotes);
    }

    public Optional<Dataset> getDatasetOptional(String name) {
        Path file = path.resolve(DATASETS).resolve(name + ".json");

        try {
            return Optional
                .ofNullable(om.create().readValue(file.toFile(), Dataset.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Dataset getDataset(String name) {
        return getDatasetOptional(name).orElseThrow(() -> DatasetNotExistingException.apply(name));
    }

    public Stream<Dataset> getDatasets() {
        ObjectMapper om = ObjectMapperFactory.apply().create();

        try {
            return Lists
                .newArrayList(Files
                    .newDirectoryStream(path.resolve(DATASETS))
                    .iterator())
                .stream()
                .map(file -> {
                    try {
                        return Optional.ofNullable(om.readValue(file.toFile(), Dataset.class));
                    } catch (IOException e) {
                        return Optional.<Dataset>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public Stream<Remote> getRemotes() {
        return getRemotes$internal().getRemotes();
    }

    public Optional<Remote> getUpstream() {
        return getRemotes$internal().getUpstream();
    }

    private Remotes getRemotes$internal() {
        final Path remotesFile = path.resolve(REMOTES);

        if (!Files.exists(remotesFile)) {
            try {
                om.create(true).writeValue(remotesFile.toFile(), Remotes.apply());
            } catch (IOException e) {
                return ExceptionUtils.wrapAndThrow(e);
            }
        }

        try {
            return om.create(true).readValue(remotesFile.toFile(), Remotes.class);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void removeRemote(String alias) {
        Remotes remotes = Remotes
            .apply()
            .remove(alias);

        updateRemotes(remotes);
    }

    public void setUpstream(String alias) {
        Remotes remotes = Remotes
            .apply()
            .setUpstream(alias);

        updateRemotes(remotes);
    }

    public void updateDataset(Dataset ds) {
        try {
            Path file = path.resolve(DATASETS).resolve(ds.getAlias().getValue() + ".json");

            if (!Files.exists(file)) {
                throw DatasetNotExistingException.apply(ds.getAlias().getValue());
            } else {
                om.create(true).writeValue(file.toFile(), ds);
            }
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    private void updateRemotes(Remotes remotes) {
        final Path remotesFile = path.resolve(REMOTES);

        try {
            om.create(true).writeValue(remotesFile.toFile(), remotes);
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public Path getPath() {
        return path.toAbsolutePath().getParent().normalize();
    }

}
