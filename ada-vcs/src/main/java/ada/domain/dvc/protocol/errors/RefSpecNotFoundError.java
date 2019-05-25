package ada.domain.dvc.protocol.errors;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.domain.dvc.values.repository.RefSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RefSpecNotFoundError implements ErrorMessage {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final RefSpec refSpec;

    @Override
    @JsonIgnore
    public String getMessage() {
        return String.format(
            "The reference %s was not found in repository %s/%s",
            refSpec, namespace.getValue(), repository.getValue());
    }

}
