package ada.vcs.client.core.project;

import ada.commons.util.Operators;
import ada.vcs.client.core.configuration.AdaConfiguration;
import ada.vcs.client.core.configuration.AdaConfigurationFactory;
import ada.vcs.client.core.dataset.Dataset;
import ada.vcs.client.core.dataset.DatasetFactory;
import ada.vcs.client.core.remotes.Remotes;
import ada.vcs.client.core.remotes.RemotesFactory;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class AdaProjectDAO {

    private final AdaConfigurationFactory configurationFactory;

    private final RemotesFactory remotesFactory;

    private final DatasetFactory datasetFactory;

    private final Path root;

    private final Path datasets;

    private final Path cache;

    private final Path local;

    private final Path remotes;

    private final Path config;

    public static AdaProjectDAO apply(
        AdaConfigurationFactory configurationFactory, RemotesFactory remotesFactory,
        DatasetFactory datasetFactory, Path root, Path datasets, Path cache, Path local, Path remotes, Path config) {

        try {
            if (!Files.exists(datasets)) {
                Files.createDirectories(datasets);
            }

            if (!Files.exists(local)) {
                Files.createDirectories(local);
            }

            if (!Files.exists(cache)) {
                Files.createDirectories(cache);
                addGitIgnore(cache, true, "Ada local cache", root);
            }

            if (!Files.exists(remotes)) {
                try (OutputStream os = Files.newOutputStream(remotes)) {
                    remotesFactory.createRemotes().writeTo(os);
                }
            }
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }

        return new AdaProjectDAO(configurationFactory, remotesFactory, datasetFactory, root, datasets, cache, local, remotes, config);
    }

    public static AdaProjectDAO apply(AdaConfigurationFactory configurationFactory, RemotesFactory remotesFactory, DatasetFactory datasetFactory, Path root) {
        Path base = root.resolve(".ada");
        Path datasets = base.resolve("datasets");
        Path local = base.resolve("local");
        Path cache = base.resolve("cache");
        Path remotes = base.resolve("remotes.json");
        Path config = base.resolve("config.json");

        return apply(configurationFactory, remotesFactory, datasetFactory, root, datasets, cache, local, remotes, config);
    }

    public void addGitIgnore(Path ignore, boolean directory, String comment) {
        if (directory) {
            addGitIgnore(root.relativize(ignore).toString() + "/**/*", comment);
        } else {
            addGitIgnore(root.relativize(ignore).toString(), comment);
        }
    }

    public void addGitIgnore(String ignorePattern, String comment) {
        addGitIgnore(ignorePattern, comment, root);
    }

    public static void addGitIgnore(Path ignore, boolean directory, String comment, Path root) {
        if (directory) {
            addGitIgnore(root.relativize(ignore).toString() + "/**/*", comment, root);
        } else {
            addGitIgnore(root.relativize(ignore).toString(), comment, root);
        }
    }

    private static void addGitIgnore(String ignorePattern, String comment, Path root) {
        Path gitignoreFile = root.resolve(".gitignore");

        try {
            if (!Files.exists(gitignoreFile)) {
                Files.createFile(gitignoreFile);
            }

            List<String> lines = Files.readAllLines(gitignoreFile);

            if (!lines.contains(ignorePattern)) {
                StringBuilder sb = new StringBuilder();

                if (!lines.isEmpty() && lines.get(lines.size() - 1).trim().equals("")) {
                    sb.append(System.lineSeparator());
                } else {
                    sb
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
                }

                if (comment != null) {
                    sb
                        .append("# ")
                        .append(comment)
                        .append(System.lineSeparator());
                }

                sb
                    .append(ignorePattern)
                    .append(System.lineSeparator());

                Files.write(gitignoreFile, sb.toString().getBytes(), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public Path getRoot() {
        return root;
    }

    public Optional<Dataset> readDataset(String name) {
        Path file = datasets.resolve(String.format("%s.json", name));
        return readDataset(file);
    }

    public AdaConfiguration readConfiguration() {
        if (Files.exists(config)) {
            return Operators.suppressExceptions(() -> configurationFactory.create(config));
        } else {
            return configurationFactory.create();
        }
    }

    private Optional<Dataset> readDataset(Path file) {
        return Operators.exceptionToNone(() -> {
            try (InputStream is = Files.newInputStream(file)) {
                return datasetFactory.createDataset(is);
            }
        });
    }

    public Stream<Dataset> readDatasets() {
        try {
            return Lists
                .newArrayList(Files
                    .newDirectoryStream(datasets)
                    .iterator())
                .stream()
                .map(this::readDataset)
                .filter(Optional::isPresent)
                .map(Optional::get);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public Remotes readRemotes() {
        try (InputStream is = Files.newInputStream(remotes)) {
            return remotesFactory.createRemotes(is);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void removeDataset(String alias) {
        Path file = datasets.resolve(String.format("%s.json", alias));

        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void saveConfiguration(AdaConfiguration configuration) {
        Operators.suppressExceptions(() -> configuration.writeTo(config));
    }

    public void saveDataset(Dataset dataset) {
        Path file = datasets.resolve(String.format("%s.json", dataset.alias().getValue()));

        try (OutputStream os = Files.newOutputStream(file)) {
            dataset.writeTo(os);
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void saveRemotes(Remotes remotes) {
        try (OutputStream os = Files.newOutputStream(this.remotes)) {
            remotes.writeTo(os);
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

}