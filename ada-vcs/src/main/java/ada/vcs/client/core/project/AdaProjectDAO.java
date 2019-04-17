package ada.vcs.client.core.project;

import ada.commons.util.Operators;
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

    private final RemotesFactory remotesFactory;

    private final DatasetFactory datasetFactory;

    private final Path root;

    private final Path datasets;

    private final Path local;

    private final Path remotes;

    public static AdaProjectDAO apply(
        RemotesFactory remotesFactory, DatasetFactory datasetFactory,
        Path root, Path datasets, Path local, Path remotes) {

        try {
            if (!Files.exists(datasets)) {
                Files.createDirectories(datasets);
            }

            if (!Files.exists(local)) {
                Files.createDirectories(local);
            }

            if (!Files.exists(remotes)) {
                try (OutputStream os = Files.newOutputStream(remotes)) {
                    remotesFactory.createRemotes().writeTo(os);
                }
            }
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }

        return new AdaProjectDAO(remotesFactory, datasetFactory, root, datasets, local, remotes);
    }

    public static AdaProjectDAO apply(RemotesFactory remotesFactory, DatasetFactory datasetFactory, Path root) {
        Path base = root.resolve(".ada");
        Path datasets = base.resolve("datasets");
        Path local = base.resolve("local");
        Path remotes = base.resolve("remotes.json");

        return apply(remotesFactory, datasetFactory, root, datasets, local, remotes);
    }

    public void addGitIgnore(Path ignore, boolean directory, String comment) {
        if (directory) {
            addGitIgnore(root.relativize(ignore).toString() + "/**/*", comment);
        } else {
            addGitIgnore(root.relativize(ignore).toString(), comment);
        }
    }

    public void addGitIgnore(String ignorePattern, String comment) {
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
