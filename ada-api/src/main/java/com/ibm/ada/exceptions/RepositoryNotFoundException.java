package com.ibm.ada.exceptions;

import com.ibm.ada.model.RepositoryName;

public class RepositoryNotFoundException extends Exception {

    private RepositoryNotFoundException(String message) {
        super(message);
    }

    public static RepositoryNotFoundException apply(RepositoryName name) {
        String message = String.format("No repository found with name '%s'", name.getValue());
        return new RepositoryNotFoundException(message);
    }

}
