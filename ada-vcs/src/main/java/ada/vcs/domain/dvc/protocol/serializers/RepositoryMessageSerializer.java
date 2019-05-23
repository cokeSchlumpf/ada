package ada.vcs.domain.dvc.protocol.serializers;

import ada.commons.databind.MessageSerializer;
import ada.vcs.domain.dvc.protocol.commands.*;
import ada.vcs.domain.dvc.protocol.events.*;
import ada.vcs.domain.dvc.protocol.queries.*;
import ada.vcs.domain.dvc.values.RepositorySummary;
import akka.actor.ExtendedActorSystem;
import com.google.common.collect.Maps;

import java.util.Map;

public final class RepositoryMessageSerializer extends MessageSerializer {

    public RepositoryMessageSerializer(ExtendedActorSystem actorSystem) {
        super(actorSystem, 2403 + 1);
    }

    @Override
    protected Map<String, Class<?>> getManifestToClass() {
        Map<String, Class<?>> m = Maps.newHashMap();

        m.put("requests/details/v1", RepositoryDetailsRequest.class);
        m.put("responses/details/v1", RepositoryDetailsResponse.class);

        m.put("requests/summary/v1", RepositorySummaryRequest.class);
        m.put("responses/summary/v1", RepositorySummaryResponse.class);

        m.put("commands/create/v1", CreateRepository.class);
        m.put("commands/grant/v1", GrantAccessToRepository.class);
        m.put("commands/pull/v1", Pull.class);
        m.put("commands/push/v1", Push.class);
        m.put("commands/remove/v1", RemoveRepository.class);
        m.put("commands/summary/v1", RepositorySummary.class);
        m.put("commands/revoke/v1", RevokedAccessFromRepository.class);
        m.put("commands/submit/v1", SubmitPushInRepository.class);

        m.put("events/created/v1", RepositoryCreated.class);
        m.put("events/granted/v1", GrantedAccessToRepository.class);
        m.put("events/removed/v1", RepositoryRemoved.class);
        m.put("events/revoked/v1", RevokedAccessFromRepository.class);
        m.put("events/upserted/v1", VersionUpsertedInRepository.class);
        m.put("events/submitted/v1", SubmittedPushInRepository.class);

        return m;
    }

}
