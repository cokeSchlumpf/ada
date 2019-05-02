package ada.vcs.client.exceptions;

import ada.commons.exceptions.AdaException;

public final class RemoteNotExistingException extends IllegalArgumentException implements AdaException {

    private RemoteNotExistingException(String message) {
        super(message);
    }

    public static RemoteNotExistingException apply(String alias) {
        String message = String.format("The remote '%s' does not exist.", alias);
        return new RemoteNotExistingException(message);
    }

}
