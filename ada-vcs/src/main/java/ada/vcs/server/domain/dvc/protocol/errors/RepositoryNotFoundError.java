package ada.vcs.server.domain.dvc.protocol.errors;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoryNotFoundError implements ErrorMessage {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    @Override
    @JsonIgnore
    public String getMessage() {
        return String.format(
            "The repository was not found %s/%s",
            namespace.getValue(), repository.getValue());
    }

}
