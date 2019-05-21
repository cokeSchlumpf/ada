package ada.vcs.domain.dvc.protocol.serializers;

import ada.commons.databind.MessageSerializer;
import akka.actor.ExtendedActorSystem;
import com.google.common.collect.Maps;

import java.util.Map;

public final class DataVersionControlSerializer extends MessageSerializer {

    public DataVersionControlSerializer(ExtendedActorSystem actorSystem) {
        super(actorSystem, 2403 + 3);
    }

    @Override
    protected Map<String, Class<?>> getManifestToClass() {
        return Maps.newHashMap();
    }

}
