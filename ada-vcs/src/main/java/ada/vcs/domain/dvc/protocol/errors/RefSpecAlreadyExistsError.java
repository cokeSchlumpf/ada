package ada.vcs.domain.dvc.protocol.errors;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.domain.shared.repository.api.RefSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RefSpecAlreadyExistsError implements ErrorMessage {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final RefSpec refSpec;

    @Override
    @JsonIgnore
    public String getMessage() {
        return String.format(
            "The reference '%s' already exists in %s/%s",
            refSpec, namespace.getValue(), repository.getValue());
    }

}
