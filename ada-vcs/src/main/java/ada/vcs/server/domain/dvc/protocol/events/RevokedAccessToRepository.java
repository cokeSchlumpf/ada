package ada.vcs.server.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.values.GrantedAuthorization;
import lombok.Value;

@Value
public final class RevokedAccessToRepository {

    private final String id;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final GrantedAuthorization authorization;

}
