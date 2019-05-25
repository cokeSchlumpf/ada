package ada.adapters.cli.exceptions;

import ada.commons.exceptions.AdaException;

public final class TargetNotExistingException extends IllegalArgumentException implements AdaException {

    private TargetNotExistingException(String message) {
        super(message);
    }

    public static TargetNotExistingException apply(String dataset, String target) {
        String message = String.format("The dataset '%s' does not contain a target with alias '%s'.", dataset, target);
        return new TargetNotExistingException(message);
    }

}
