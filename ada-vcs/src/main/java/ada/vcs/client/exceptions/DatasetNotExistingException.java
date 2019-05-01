package ada.vcs.client.exceptions;

public final class DatasetNotExistingException extends IllegalArgumentException {

    private DatasetNotExistingException(String message) {
        super(message);
    }

    public static DatasetNotExistingException apply(String name) {
        String message = String.format("The dataset '%s' does not exist.", name);
        return new DatasetNotExistingException(message);
    }

}
