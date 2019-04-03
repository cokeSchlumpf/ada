package ada.vcs.client.core;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.client.exceptions.DatasetAlreadyExistsException;
import ada.vcs.client.exceptions.DatasetNotExistingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Value
@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class AdaProject {

    private static final String ADA_DIR = ".ada";

    private static final String LOCAL = "local";

    private static final String DATASETS = "datasets";

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
            Path dir = where.resolve(ADA_DIR);
            if (!Files.exists(dir)) Files.createDirectory(dir);

            Path gitignore = where.resolve(".gitignore");
            Path local = where.relativize(dir.resolve(LOCAL));

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

            return AdaProject.apply(dir, ObjectMapperFactory.apply());
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
        Dataset ds = getDataset(dataset).orElseThrow(() -> DatasetNotExistingException.apply(dataset));

        Map<String, Target> targets$next = Maps.newHashMap();
        ds.getTargets().forEach(t -> targets$next.put(t.getAlias().getValue(), t));
        targets$next.put(target.getAlias().getValue(), target);

        updateDataset(Dataset.apply(ds.getAlias(), ds.getSource(), ds.getSchema(), targets$next));
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

    public Optional<Dataset> getDataset(String name) {
        Path file = path.resolve(DATASETS).resolve(name + ".json");

        try {
            return Optional.ofNullable(om.create().readValue(file.toFile(), Dataset.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Stream<Dataset> getDatasets() {
        ObjectMapper om = ObjectMapperFactory.apply().create();

        try {
            return Lists
                .newArrayList(Files
                    .newDirectoryStream(path)
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

    public Path getPath() {
        return path.toAbsolutePath().normalize();
    }

}
