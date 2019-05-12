package ada.vcs.client.core.project;

import ada.commons.util.ProcessUtils;
import ada.commons.util.ResourceName;
import ada.vcs.client.core.AdaHome;
import ada.vcs.client.core.configuration.AdaConfiguration;
import ada.vcs.client.core.endpoints.Endpoint;
import ada.vcs.shared.repository.api.User;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
public final class AdaProjectConfiguration implements AdaConfiguration {

    private final AdaProjectDAO dao;

    private final AdaHome home;

    @Override
    public Optional<Endpoint> getEndpoint() {
        return home.getConfiguration().getEndpoint();
    }

    @Override
    public Optional<Endpoint> getEndpoint(ResourceName alias) {
        return home.getConfiguration().getEndpoint(alias);
    }

    @Override
    public Optional<User> getUser() {
        return Optional.ofNullable(getUser$project().orElseGet(() -> getUser$global().orElse(null)));
    }

    @Override
    public void addEndpoint(Endpoint endpoint) {
        home.getConfiguration().addEndpoint(endpoint);
    }

    @Override
    public List<Endpoint> getEndpoints() {
        return home.getConfiguration().getEndpoints();
    }

    @Override
    public void setEndpoint(ResourceName alias) {
        home.getConfiguration().setEndpoint(alias);
    }

    private Optional<User> getUser$project() {
        return dao.readConfiguration().getUser();
    }

    private Optional<User> getUser$global() {
        return Optional.ofNullable(home
            .getConfiguration()
            .getUser()
            .orElseGet(() -> {
                ProcessUtils process = ProcessUtils.apply(dao.getRoot());

                return process
                    .exec("git config user.name")
                    .map(username -> process
                        .exec("git config user.email")
                        .map(mail -> User.apply(username.trim(), mail.trim()))
                        .orElse(User.apply(username)))
                    .orElse(null);
            }));
    }

    @Override
    public void setUser(User user) {
        dao.readConfiguration().setUser(user);
    }

    @Override
    public void removeEndpoint(ResourceName alias) {
        home.getConfiguration().removeEndpoint(alias);
    }

    @Override
    public void unsetUser() {
        dao.readConfiguration().unsetUser();
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        dao.readConfiguration().writeTo(os);
    }

}
