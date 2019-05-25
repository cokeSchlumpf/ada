package ada.adapters.cli.exceptions;

import ada.commons.exceptions.AdaException;

public final class NoUserConfiguredException extends RuntimeException implements AdaException {

    private NoUserConfiguredException(String message) {
        super(message);
    }

    public static NoUserConfiguredException apply() {
        String msg = "No user configured for current project.";
        return new NoUserConfiguredException(msg);
    }

}
