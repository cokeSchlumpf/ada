package ada.vcs.client.consoles;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class CommandLineConsole {

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
        return new PrintStream(CommandLineOutputStream.apply(this));
    }

    protected abstract void print(String message);

    protected abstract void println(String message);

    @AllArgsConstructor(staticName = "apply")
    private static class CommandLineOutputStream extends OutputStream {

        private final CommandLineConsole console;

        @Override
        public void write(int b) throws IOException {
            console.print(new String(new byte[] { (byte) b }));
        }

    }

}
