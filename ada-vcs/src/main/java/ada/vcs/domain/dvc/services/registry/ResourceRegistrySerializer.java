package ada.vcs.domain.dvc.services.registry;

import ada.commons.databind.MessageSerializer;
import akka.actor.ExtendedActorSystem;
import com.google.common.collect.Maps;

import java.util.Map;

public final class ResourceRegistrySerializer extends MessageSerializer {

    public ResourceRegistrySerializer(ExtendedActorSystem actorSystem) {
        super(actorSystem, 2403 + 5);
    }

    @Override
    protected Map<String, Class<?>> getManifestToClass() {
        Map<String, Class<?>> m = Maps.newHashMap();

        m.put("commands/register/v1", RegisterResource.class);
        m.put("commands/remove/v1", RemoveResource.class);

        m.put("events/registered/v1", ResourceRegistered.class);
        m.put("events/removed/v1", ResourceRemoved.class);

        return m;
    }

}
