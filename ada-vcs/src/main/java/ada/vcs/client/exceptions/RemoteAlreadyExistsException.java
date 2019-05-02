package ada.vcs.client.exceptions;

import ada.commons.exceptions.AdaException;

public final class RemoteAlreadyExistsException extends IllegalArgumentException implements AdaException {

    private RemoteAlreadyExistsException(String message) {
        super(message);
    }

    public static RemoteAlreadyExistsException apply(String name) {
        String message = String.format("A remote with the name '%s' already exists", name);
        return new RemoteAlreadyExistsException(message);
    }

}
