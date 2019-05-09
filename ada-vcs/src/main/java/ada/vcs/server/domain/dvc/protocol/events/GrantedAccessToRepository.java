package ada.vcs.server.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.values.GrantedAuthorization;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class GrantedAccessToRepository {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final GrantedAuthorization authorization;

}
