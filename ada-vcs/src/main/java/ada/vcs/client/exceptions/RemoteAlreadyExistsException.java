package ada.vcs.client.exceptions;

public class RemoteAlreadyExistsException extends IllegalArgumentException {

    private RemoteAlreadyExistsException(String message) {
        super(message);
    }

    public static RemoteAlreadyExistsException apply(String name) {
        String message = String.format("A remote with the name '%s' already exists", name);
        return new RemoteAlreadyExistsException(message);
    }

}
