package ada.vcs.client.exceptions;

public class TargetNotExistingException extends IllegalArgumentException {

    private TargetNotExistingException(String message) {
        super(message);
    }

    public static TargetNotExistingException apply(String dataset, String target) {
        String message = String.format("The dataset '%s' does not contain a target with alias '%s'.", dataset, target);
        return new TargetNotExistingException(message);
    }

}
