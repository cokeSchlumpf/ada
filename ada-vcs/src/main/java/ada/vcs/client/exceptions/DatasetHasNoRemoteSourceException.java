package ada.vcs.client.exceptions;

public final class DatasetHasNoRemoteSourceException extends IllegalArgumentException implements AdaException {

    private DatasetHasNoRemoteSourceException(String message) {
        super(message);
    }

    public static DatasetHasNoRemoteSourceException apply(String name) {
        String message = String.format("The dataset '%s' has not been pushed to a remote yet.", name);
        return new DatasetHasNoRemoteSourceException(message);
    }

}
