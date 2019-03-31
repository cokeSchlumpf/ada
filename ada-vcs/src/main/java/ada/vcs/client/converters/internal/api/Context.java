package ada.vcs.client.converters.internal.api;

import java.io.IOException;
import java.io.OutputStream;

public interface Context {

    void writeTo(OutputStream os) throws IOException;

}
