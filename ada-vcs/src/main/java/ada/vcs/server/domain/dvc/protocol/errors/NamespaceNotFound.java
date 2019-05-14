package ada.vcs.server.domain.dvc.protocol.errors;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class NamespaceNotFound implements ErrorMessage {

    private final String id;

    private final ResourceName namespace;

    @Override
    @JsonIgnore
    public String getMessage() {
        return String.format("The namespace was not found %s", namespace.getValue());
    }

}
