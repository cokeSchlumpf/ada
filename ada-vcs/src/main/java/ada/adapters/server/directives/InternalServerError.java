package ada.adapters.server.directives;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class InternalServerError implements ServerError {

    @Override
    @JsonIgnore
    public String getMessage() {
        return "¯\\_(ツ)_/¯ Ups... an internal server error occurred on Ada endpoint.";
    }

}
