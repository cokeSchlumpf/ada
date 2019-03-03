package com.ibm.ada.exceptions;

public final class InvalidVersionStringException extends RuntimeException {

    private InvalidVersionStringException(String message) {
        super(message);
    }

    public static InvalidVersionStringException apply(String versionString) {
        String message = String.format("The value '%s' is not a valid version.", versionString);
        return new InvalidVersionStringException(message);
    }

}
