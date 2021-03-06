package ada.domain.dvc.protocol.serializers;

import ada.commons.databind.MessageSerializer;
import ada.domain.dvc.protocol.events.NamespaceCreated;
import ada.domain.dvc.protocol.events.NamespaceRemoved;
import ada.domain.dvc.protocol.queries.RepositoriesInNamespaceRequest;
import ada.domain.dvc.protocol.queries.RepositoriesInNamespaceResponse;
import ada.domain.dvc.protocol.commands.CreateNamespace;
import ada.domain.dvc.protocol.commands.RemoveNamespace;
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

        m.put("requests/repositories/v1", RepositoriesInNamespaceRequest.class);
        m.put("responses/repositories/v1", RepositoriesInNamespaceResponse.class);

        m.put("commands/create/v1", CreateNamespace.class);
        m.put("commands/remove/v1", RemoveNamespace.class);

        m.put("events/created/v1", NamespaceCreated.class);
        m.put("events/removed/v1", NamespaceRemoved.class);

        return m;
    }

}
