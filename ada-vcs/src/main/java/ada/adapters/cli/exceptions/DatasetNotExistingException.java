package ada.adapters.cli.exceptions;

import ada.commons.exceptions.AdaException;

public final class DatasetNotExistingException extends IllegalArgumentException implements AdaException {

    private DatasetNotExistingException(String message) {
        super(message);
    }

    public static DatasetNotExistingException apply(String name) {
        String message = String.format("The dataset '%s' does not exist.", name);
        return new DatasetNotExistingException(message);
    }

}
