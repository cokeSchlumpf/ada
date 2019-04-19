package ada.vcs.client.core;

import java.io.IOException;
import java.io.OutputStream;

public interface Writable {

    void writeTo(OutputStream os) throws IOException;

}
