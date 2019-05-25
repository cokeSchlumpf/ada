package ada.adapters.cli;

import ada.adapters.cli.core.project.AdaProject;
import ada.adapters.cli.core.project.AdaProjectFactory;
import ada.adapters.cli.util.AbstractAdaTest;
import com.google.common.collect.Lists;
import org.assertj.core.util.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AdaProjectUTest extends AbstractAdaTest {

    @Test
    public void initTest() throws IOException {
        File tmp = Files.newTemporaryFolder();

        try {
            AdaProject root = getContext()
                .factories()
                .projectFactory()
                .init(tmp.toPath());

            List<String> files = Lists
                .newArrayList(java.nio.file.Files.newDirectoryStream(tmp.toPath()).iterator())
                .stream()
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());

            assertThat(files)
                .contains(".ada");
        } finally {
            Files.delete(tmp);
        }
    }

    @Test
    public void applyTest() throws IOException {
        File tmp = Files.newTemporaryFolder();

        try {
            AdaProjectFactory factory = getContext()
                .factories()
                .projectFactory();

            AdaProject root = factory
                .init(tmp.toPath());

            Path subDir = tmp.toPath().resolve("foo").resolve("bar");
            java.nio.file.Files.createDirectories(subDir);

            Optional<AdaProject> detected = factory.from(subDir);

            assertThat(detected.isPresent()).isTrue();
            detected.ifPresent(adaProject -> assertThat(adaProject).isEqualTo(root));
        } finally {
            Files.delete(tmp);
        }
    }

}
