package ada.vcs.client.converters.internal.contexts;

import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor(staticName = "apply")
public final class FileContextFactory {

    public FileContext create(InputStream is) throws IOException {
        return FileContext.apply(IOUtils.toString(is, StandardCharsets.UTF_8));
    }

}
