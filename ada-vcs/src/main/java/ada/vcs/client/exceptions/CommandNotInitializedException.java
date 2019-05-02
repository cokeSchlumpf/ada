package ada.vcs.client.exceptions;

public final class CommandNotInitializedException extends IllegalStateException {

    private CommandNotInitializedException(String message) {
        super(message);
    }

    public static CommandNotInitializedException apply() {
        return new CommandNotInitializedException("The command was not properly initialized.");
    }

}
