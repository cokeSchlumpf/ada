package ada.vcs.client.exceptions;

public final class ExitWithErrorException extends IllegalArgumentException implements AdaException {

    private int exitCode;

    private ExitWithErrorException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public static ExitWithErrorException apply(String message, Object ...args) {
        return new ExitWithErrorException(String.format(message, args), -1);
    }

    public static ExitWithErrorException apply(int exitCode, String message) {
        return new ExitWithErrorException(message, exitCode);
    }

    public int getExitCode() {
        return exitCode;
    }

}
