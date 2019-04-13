package ada.vcs.client.core.project;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.Operators;
import ada.vcs.client.core.Dataset;
import ada.vcs.client.core.remotes.Remotes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class AdaProjectDAO {

    private final Path root;

    private final Path datasets;

    private final Path remotes;

    private final ObjectMapper om;

    public static AdaProjectDAO apply(Path root, Path datasets, Path remotes, ObjectMapper om) {
        try {
            if (!Files.exists(datasets)) {
                Files.createDirectories(datasets);
            }

            if (!Files.exists(remotes)) {
                om.writeValue(remotes.toFile(), Remotes.apply());
            }
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }

        return new AdaProjectDAO(root, datasets, remotes, om);
    }

    public static AdaProjectDAO apply(Path root) {
        Path base = root.resolve(".ada");
        Path datasets = base.resolve("datasets");
        Path remotes = base.resolve("remotes.json");

        ObjectMapper om = ObjectMapperFactory.apply().create(true);

        return apply(root, datasets, remotes, om);
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
        return Operators.exceptionToNone(() -> om.readValue(file.toFile(), Dataset.class));
    }

    public Stream<Dataset> readDatasets() {
        try {
            return Lists
                .newArrayList(Files
                    .newDirectoryStream(datasets)
                    .iterator())
                .stream()
                .map(file -> Operators
                    .exceptionToNone(() -> om.readValue(file.toFile(), Dataset.class)))
                .filter(Optional::isPresent)
                .map(Optional::get);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public Remotes readRemotes() {
        try {
            return om.readValue(remotes.toFile(), Remotes.class);
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
        try {
            Path file = datasets.resolve(String.format("%s.json", dataset.getAlias().getValue()));
            om.writeValue(file.toFile(), dataset);
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void saveRemotes(Remotes remotes) {
        try {
            om.writeValue(this.remotes.toFile(), remotes);
        } catch (IOException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

}
