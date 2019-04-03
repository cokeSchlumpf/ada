package ada.vcs.client.exceptions;

public class DatasetNotExistingException extends IllegalArgumentException {

    private DatasetNotExistingException(String message) {
        super(message);
    }

    public static DatasetNotExistingException apply(String name) {
        String message = String.format("The dataset '%s' does not exist.");
        return new DatasetNotExistingException(message);
    }

}