package ada.vcs.client.exceptions;

public final class DatasetAlreadyExistsException extends IllegalArgumentException implements AdaException {

    private DatasetAlreadyExistsException(String message) {
        super(message);
    }

    public static DatasetAlreadyExistsException apply(String name) {
        String message = String.format("A dataset with the name '%s' already exists", name);
        return new DatasetAlreadyExistsException(message);
    }

}
