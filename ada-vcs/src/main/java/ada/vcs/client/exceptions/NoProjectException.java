package ada.vcs.client.exceptions;

public class NoProjectException extends RuntimeException {

    private NoProjectException(String message) {
        super(message);
    }

    public static NoProjectException apply() {
        String msg = "No project initialized in current directory. Use `init` to initialize directory.";
        return new NoProjectException(msg);
    }

}
