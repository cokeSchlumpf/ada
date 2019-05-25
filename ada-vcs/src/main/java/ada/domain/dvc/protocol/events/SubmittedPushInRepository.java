package ada.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.RepositoryEvent;
import ada.domain.dvc.values.UserId;
import ada.domain.legacy.repository.api.RefSpec;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class SubmittedPushInRepository implements RepositoryEvent {

    private final String id;

    private final UserId user;

    private final ResourceName namespace;

    private final ResourceName repository;

    private final RefSpec.VersionRef refSpec;

}
