package ada.cli.exceptions;

public class ClientException extends RuntimeException {

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
