package ada.adapters.server.directives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessageServerError implements ServerError {

    private final String message;

    @JsonCreator
    public static ErrorMessageServerError apply(@JsonProperty("message") String message) {
        return new ErrorMessageServerError(message);
    }

}
