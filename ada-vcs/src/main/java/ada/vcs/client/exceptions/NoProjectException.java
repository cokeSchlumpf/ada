package ada.vcs.client.exceptions;

import ada.commons.exceptions.AdaException;

public final class NoProjectException extends RuntimeException implements AdaException {

    private NoProjectException(String message) {
        super(message);
    }

    public static NoProjectException apply() {
        String msg = "No project initialized in current directory. Use `init` to initialize directory.";
        return new NoProjectException(msg);
    }

}
