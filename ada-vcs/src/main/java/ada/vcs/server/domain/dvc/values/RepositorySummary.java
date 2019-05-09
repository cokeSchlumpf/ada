package ada.vcs.server.domain.dvc.values;

import ada.commons.util.ResourceName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(staticName = "apply")
public class RepositorySummary {

    private final ResourceName namespace;

    private final ResourceName repository;

    private final Date lastUpdate;

    private final String latestId;

}
