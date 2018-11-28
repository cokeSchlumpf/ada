package ada.client.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class StringOutput implements Output {

    private static final Logger LOG = LoggerFactory.getLogger(StringOutput.class);

    private final ByteArrayOutputStream baos;

    private final PrintStreamOutput out;

    private StringOutput(ByteArrayOutputStream baos, PrintStreamOutput out) {
        this.baos = baos;
        this.out = out;
    }

    public static StringOutput apply(ByteArrayOutputStream baos, PrintStreamOutput out) {
        return new StringOutput(baos, out);
    }

    public static StringOutput apply() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.toString());
            PrintStreamOutput out = PrintStreamOutput.apply(ps);
            return apply(baos, out);
        } catch (UnsupportedEncodingException exception) {
            LOG.error(
                "UTF-8 doesn't seem to be a supported encoding on your system - Will fallback to use System.out PrintStream",
                exception);

            PrintStreamOutput out = PrintStreamOutput.apply(System.out);
            return apply(baos, out);
        }
    }

    @Override
    public void exception(Throwable e) {
        e.printStackTrace(new PrintStream(baos));
    }

    @Override
    public void message(String message, Object... args) {
        out.message(message, args);
    }

    @Override
    public void message(String message) {
        out.message(message);
    }

    @Override
    public void separator() {
        out.separator();
    }

    @Override
    public void table(String[] headers, String[][] content) {
        out.table(headers, content);
    }

    @Override
    public PrintStream getStream() {
        return out.getStream();
    }

    @Override
    public String toString() {
        try {
            return baos.toString(StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            LOG.error("UTF-8 is required to be a known encoding on your system.", e);
            return e.getMessage();
        }
    }

}
