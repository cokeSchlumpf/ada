package ada.vcs.adapters.cli.consoles;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public abstract class CommandLineConsole implements Output {

    public static CommandLineConsole apply() {
        return SystemCommandLineConsole.apply();
    }

    public static CommandLineConsole apply(StringBuffer buffer) {
        return BufferedCommandLineConsole.apply(buffer);
    }

    public void message(String message, Object... args) {
        println(String.format(message, args));
    }

    public PrintStream printStream() {
        try {
            return new PrintStream(CommandLineOutputStream.apply(this), true, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    @AllArgsConstructor(staticName = "apply")
    private static class CommandLineOutputStream extends OutputStream {

        private final CommandLineConsole console;

        @Override
        public void write(int b) {
            console.print(new String(new byte[] { (byte) b }, StandardCharsets.UTF_8));
        }

    }

}
