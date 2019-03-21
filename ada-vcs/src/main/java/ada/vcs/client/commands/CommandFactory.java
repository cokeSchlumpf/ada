package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

@AllArgsConstructor(staticName = "apply")
public final class CommandFactory implements CommandLine.IFactory {

    private final CommandLineConsole console;

    @Override
    @SuppressWarnings("unused")
    public <K> K create(Class<K> cls) throws Exception {
        if (cls.equals(InitCommand.class)) {
            return (K) InitCommand.apply(console);
        } else {
            throw new Exception("Unknown class " + cls);
        }
    }

    public CommandLineConsole getConsole() {
        return console;
    }

}
