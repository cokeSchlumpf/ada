package ada.vcs.client.util;

import ada.commons.util.Operators;
import ada.commons.io.Writable;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor(staticName = "apply")
public final class WritableUtil<T extends Writable> {

    private T writable;

    private Operators.ExceptionalFunction<InputStream, T> factory;

    public String writeAsString() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            writable.writeTo(baos);

            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public T readValueFromString(String value) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(value.getBytes())) {
            return factory.apply(is);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

}
