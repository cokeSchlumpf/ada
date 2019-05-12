package ada.vcs.client.exceptions;

import ada.commons.exceptions.AdaException;

public final class EndpointNotExistingException extends IllegalArgumentException implements AdaException {

    private EndpointNotExistingException(String message) {
        super(message);
    }

    public static EndpointNotExistingException apply(String name) {
        String message = String.format("The endpoint '%s' does not exist.", name);
        return new EndpointNotExistingException(message);
    }

}
