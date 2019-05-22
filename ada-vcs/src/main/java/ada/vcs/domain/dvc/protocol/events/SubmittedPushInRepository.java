package ada.vcs.domain.dvc.protocol.events;

import ada.commons.util.ResourceName;
import ada.vcs.domain.dvc.protocol.api.RepositoryEvent;
import ada.vcs.domain.dvc.values.UserId;
import ada.vcs.domain.legacy.repository.api.RefSpec;
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
