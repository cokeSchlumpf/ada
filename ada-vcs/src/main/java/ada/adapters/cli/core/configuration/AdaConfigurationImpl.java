package ada.adapters.cli.core.configuration;

import ada.adapters.cli.exceptions.EndpointNotExistingException;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.domain.dvc.values.repository.User;
import ada.adapters.cli.core.endpoints.Endpoint;
import ada.adapters.cli.core.endpoints.EndpointMemento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
public final class AdaConfigurationImpl implements AdaConfiguration {

    private final ObjectMapper om;

    private final Path output;

    private ResourceName currentEndpoint;

    private ResourceName namespace;

    private User user;

    private Map<ResourceName, Endpoint> endpoints;

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    @Override
    public void addEndpoint(Endpoint endpoint) {
        endpoints.put(endpoint.getAlias(), endpoint);

        if (endpoints.size() == 1) {
            currentEndpoint = endpoint.getAlias();
        }

        save();
    }

    @Override
    public Optional<Endpoint> getEndpoint() {
        if (currentEndpoint != null) {
            return Optional.ofNullable(endpoints.get(currentEndpoint));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Endpoint> getEndpoint(ResourceName alias) {
        return Optional.ofNullable(endpoints.get(alias));
    }

    @Override
    public Optional<ResourceName> getNamespace() {
        return Optional.ofNullable(namespace);
    }

    @Override
    public List<Endpoint> getEndpoints() {
        return Lists.newArrayList(endpoints.values());
    }

    @Override
    public void setEndpoint(ResourceName alias) {
        if (endpoints.get(alias) == null) {
            throw EndpointNotExistingException.apply(alias.getValue());
        } else {
            currentEndpoint = alias;
            save();
        }
    }

    @Override
    public void setNamespace(ResourceName namespace) {
        this.namespace = namespace;
        save();
    }

    @Override
    public void setUser(User user) {
        this.user = user;
        save();
    }

    @Override
    public void removeEndpoint(ResourceName alias) {
        endpoints.remove(alias);

        if (currentEndpoint != null && currentEndpoint.equals(alias)) {
            currentEndpoint = null;

            if (endpoints.size() > 0) {
                currentEndpoint = Lists.newArrayList(endpoints.keySet()).get(0);
            }
        }

        save();
    }

    @Override
    public void unsetNamespace() {
        this.namespace = null;
        save();
    }

    @Override
    public void unsetUser() {
        this.user = null;
        save();
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        List<EndpointMemento> endpoints = Lists.newArrayList();
        this.endpoints.values().stream().map(Endpoint::memento).forEach(endpoints::add);

        om.writeValue(os, AdaConfigurationMemento.apply(user, currentEndpoint, namespace, endpoints));
    }
    
    private void save() {
        Operators.suppressExceptions(() -> writeTo(output));
    }

}
