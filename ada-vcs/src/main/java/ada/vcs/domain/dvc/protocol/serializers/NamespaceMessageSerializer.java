package ada.vcs.domain.dvc.protocol.serializers;

import ada.commons.databind.MessageSerializer;
import ada.vcs.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.domain.dvc.protocol.events.RepositoryRemoved;
import akka.actor.ExtendedActorSystem;
import com.google.common.collect.Maps;

import java.util.Map;

public final class NamespaceMessageSerializer extends MessageSerializer {

    protected NamespaceMessageSerializer(ExtendedActorSystem actorSystem) {
        super(actorSystem, 2403 + 2);
    }

    @Override
    protected Map<String, Class<?>> getManifestToClass() {
        Map<String, Class<?>> m = Maps.newHashMap();

        m.put("created/v1", RepositoryCreated.class);
        m.put("removed/v1", RepositoryRemoved.class);

        return m;
    }

}
