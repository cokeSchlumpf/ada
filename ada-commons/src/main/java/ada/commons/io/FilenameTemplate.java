package ada.commons.io;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class FilenameTemplate {

    private final String prefix;

    private final String extension;

    public String getTemplate() {
        return String.format("%s-%%s%%s.%s", prefix, extension);
    }

}
