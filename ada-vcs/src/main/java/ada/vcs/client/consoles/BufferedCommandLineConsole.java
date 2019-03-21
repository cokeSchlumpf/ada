package ada.vcs.client.consoles;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
final class BufferedCommandLineConsole extends CommandLineConsole {

    private final StringBuffer buffer;

    @Override
    protected void print(String message) {
        buffer.append(message);
    }

    @Override
    protected void println(String message) {
        buffer.append(message);
        buffer.append(System.lineSeparator());
    }

}
