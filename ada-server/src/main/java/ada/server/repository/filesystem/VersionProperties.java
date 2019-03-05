package ada.server.repository.filesystem;

import com.ibm.ada.api.model.Schema;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Date;

@Value
@Wither
@AllArgsConstructor(staticName = "apply")
public class VersionProperties {

    // TODO: Commit Properties?

    private final boolean appended;

    private final long recordCount;

    private final long byteSize;

    private final Date created;

    private final Date updated;

    private final Schema schema;

    @SuppressWarnings("unused")
    private VersionProperties() {
        this(false, 0L, 0L, new Date(), new Date(), null);
    }

    public static VersionProperties apply(boolean appended, Schema schema) {
        return apply(appended, 0, 0, new Date(), new Date(), schema);
    }

}
