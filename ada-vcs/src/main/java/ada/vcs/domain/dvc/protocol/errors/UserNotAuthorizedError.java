package ada.vcs.domain.dvc.protocol.errors;

import ada.commons.util.ErrorMessage;
import ada.vcs.domain.dvc.protocol.values.User;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class UserNotAuthorizedError implements ErrorMessage {

    private final String id;

    private final User user;

    @Override
    public String getMessage() {
        return "You are not authorized to execute the operation.";
    }

}
