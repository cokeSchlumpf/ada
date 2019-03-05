package ada.cli;

import ada.cli.commands.about.AboutCommand;
import ada.cli.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import lombok.Value;
import picocli.CommandLine;

@Value
@AllArgsConstructor(staticName = "apply")
public class CommandFactory implements CommandLine.IFactory {

    private final CommandLineConsole console;

    @Override
    @SuppressWarnings("unchecked")
    public <K> K create(Class<K> cls) throws Exception {
        if (cls.equals(AboutCommand.class)) {
            return (K) AboutCommand.apply(console);
        } else {
            throw InstantiationException.apply(cls);
        }
    }

    CommandLineConsole console() {
        return console;
    }

    private static class InstantiationException extends Exception {

        private InstantiationException(String message) {
            super(message);
        }

        public static InstantiationException apply(Class<?> clazz) {
            String message = String.format(
                "Cannot instantiate class of '%s'; class needs to be implemented in '%s'",
                clazz.getName(), CommandFactory.class.getName());

            return new InstantiationException(message);
        }

    }
}
