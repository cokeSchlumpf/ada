package ada.experiment;

import ada.commons.databind.MessageSerializer;
import akka.actor.ExtendedActorSystem;
import com.google.common.collect.Maps;

import java.util.Map;

public final class ExperimentMessageSerializer extends MessageSerializer {

    protected ExperimentMessageSerializer(ExtendedActorSystem actorSystem) {
        super(actorSystem, 2503);
    }

    @Override
    protected Map<String, Class<?>> getManifestToClass() {
        Map<String, Class<?>> m = Maps.newHashMap();
        m.put("increment", Increment.class);
        m.put("get", GetValue.class);
        return m;
    }

}
