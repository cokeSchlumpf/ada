package ada.adapters.cli.exceptions;

import ada.commons.exceptions.AdaException;

public final class NoEndpointException extends RuntimeException implements AdaException {

    private NoEndpointException(String message) {
        super(message);
    }

    public static NoEndpointException apply() {
        String msg = "No endpoint configured, use 'ada endpoints add' to add an endpoint.";
        return new NoEndpointException(msg);
    }

}
