package ada.vcs.server.domain.dvc.protocol.serializers;

import ada.commons.databind.MessageSerializer;
import ada.vcs.server.domain.dvc.protocol.events.NamespaceCreated;
import ada.vcs.server.domain.dvc.protocol.events.NamespaceRemoved;
import akka.actor.ExtendedActorSystem;
import com.google.common.collect.Maps;

import java.util.Map;

public final class DataVersionControlSerializer extends MessageSerializer {

    public DataVersionControlSerializer(ExtendedActorSystem actorSystem) {
        super(actorSystem, 2403 + 3);
    }

    @Override
    protected Map<String, Class<?>> getManifestToClass() {
        Map<String, Class<?>> m = Maps.newHashMap();

        m.put("created/v1", NamespaceCreated.class);
        m.put("removed/v1", NamespaceRemoved.class);

        return m;
    }

}
