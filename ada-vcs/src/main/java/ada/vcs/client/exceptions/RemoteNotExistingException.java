package ada.vcs.client.exceptions;

public final class RemoteNotExistingException extends IllegalArgumentException {

    private RemoteNotExistingException(String message) {
        super(message);
    }

    public static RemoteNotExistingException apply(String alias) {
        String message = String.format("The remote '%s' does not exist.", alias);
        return new RemoteNotExistingException(message);
    }

}
