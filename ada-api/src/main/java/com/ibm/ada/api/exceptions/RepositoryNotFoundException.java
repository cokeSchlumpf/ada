package com.ibm.ada.api.exceptions;

import com.ibm.ada.model.ResourceName;

public class RepositoryNotFoundException extends Exception {

    private RepositoryNotFoundException(String message) {
        super(message);
    }

    public static RepositoryNotFoundException apply(ResourceName name) {
        String message = String.format("No repository found with name '%s'", name.getValue());
        return new RepositoryNotFoundException(message);
    }

}
