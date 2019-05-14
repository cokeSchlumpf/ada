package ada.vcs.adapters.cli.consoles;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
final class BufferedCommandLineConsole extends CommandLineConsole {

    private final StringBuffer buffer;

    @Override
    public void print(String message) {
        buffer.append(message);
    }

    @Override
    public void println(String message) {
        buffer.append(message);
        buffer.append(System.lineSeparator());
    }

}
