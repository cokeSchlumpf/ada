package ada.vcs.client;

import ada.vcs.client.core.project.AdaProjectTemp;
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

public class AdaProjectUTest {

    @Test
    public void initTest() throws IOException {
        File tmp = Files.newTemporaryFolder();

        try {
            AdaProjectTemp root = AdaProjectTemp.init(tmp.toPath());
            System.out.println(root);

            List<String> files = Lists
                .newArrayList(java.nio.file.Files.newDirectoryStream(tmp.toPath()).iterator())
                .stream()
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());

            assertThat(files)
                .contains(".gitignore")
                .contains(".ada");
        } finally {
            Files.delete(tmp);
        }
    }

    @Test
    public void applyTest() throws IOException {
        File tmp = Files.newTemporaryFolder();

        try {
            AdaProjectTemp root = AdaProjectTemp.init(tmp.toPath());

            Path subDir = tmp.toPath().resolve("foo").resolve("bar");
            java.nio.file.Files.createDirectories(subDir);

            Optional<AdaProjectTemp> detected = AdaProjectTemp.from(subDir);

            assertThat(detected.isPresent()).isTrue();
            detected.ifPresent(adaProject -> assertThat(adaProject).isEqualTo(root));
        } finally {
            Files.delete(tmp);
        }
    }

}
