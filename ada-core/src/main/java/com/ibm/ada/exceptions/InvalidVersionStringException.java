package com.ibm.ada.exceptions;

public final class InvalidVersionStringException extends RuntimeException {

    private InvalidVersionStringException(String message) {
        super(message);
    }

    public static InvalidVersionStringException apply(String versionString) {
        // TODO
        return new InvalidVersionStringException("TODO");
    }

}
