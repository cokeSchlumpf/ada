package ada.vcs.client.core.configuration;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.endpoints.Endpoint;
import ada.vcs.client.core.endpoints.EndpointFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply")
public final class AdaConfigurationFactory {

    ObjectMapper om;

    EndpointFactory endpointFactory;

    public AdaConfiguration create(Path path, AdaConfigurationMemento memento) {
        List<Endpoint> endpoints = memento
            .getEndpoints()
            .stream()
            .map(endpointFactory::create)
            .collect(Collectors.toList());

        Map<ResourceName, Endpoint> endpointMap = Maps.newHashMap();
        endpoints.forEach(e -> endpointMap.put(e.getAlias(), e));

        return AdaConfigurationImpl.apply(om, path, memento.getEndpoint(), memento.getUser(), endpointMap);
    }

    public AdaConfiguration create(Path path, InputStream is) throws IOException {
        AdaConfigurationMemento memento = om.readValue(is, AdaConfigurationMemento.class);
        return create(path, memento);
    }

    public AdaConfiguration create(Path path) throws IOException {
        if (Files.exists(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                return create(path, is);
            }
        } else {
            return AdaConfigurationImpl.apply(om, path, null, null, Maps.newHashMap());
        }
    }

}
