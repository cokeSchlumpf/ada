package ada.commons.util;

import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
public final class ProcessUtils {

    private final Path dir;

    public Optional<String> exec(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            pb.directory(dir.toFile());
            Process p = pb.start();

            String result = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
            if (result != null && result.trim().length() > 0) {
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
