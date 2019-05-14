package ada.vcs.server.domain.dvc.protocol.serializers;

import ada.commons.databind.MessageSerializer;
import ada.vcs.server.domain.dvc.protocol.events.GrantedAccessToRepository;
import ada.vcs.server.domain.dvc.protocol.events.RepositoryInitialized;
import ada.vcs.server.domain.dvc.protocol.events.RevokedAccessToRepository;
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

        m.put("granted/v1", GrantedAccessToRepository.class);
        m.put("initialized/v1", RepositoryInitialized.class);
        m.put("revoked/v1", RevokedAccessToRepository.class);

        return m;
    }

}
