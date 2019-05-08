package ada.vcs.client.core.project;

import ada.commons.util.ProcessUtils;
import ada.vcs.client.core.AdaHome;
import ada.vcs.client.core.configuration.AdaConfiguration;
import ada.vcs.shared.repository.api.User;
import lombok.AllArgsConstructor;

import java.io.OutputStream;
import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
public final class AdaProjectConfiguration implements AdaConfiguration {

    private final AdaProjectDAO dao;

    private final AdaHome home;

    @Override
    public Optional<User> getUser() {
        return dao
            .readConfiguration()
            .getUser()
            .map(Optional::of)
            .orElseGet(() -> home
                .getConfiguration()
                .getUser()
                .map(Optional::of)
                .orElseGet(() -> {
                    ProcessUtils process = ProcessUtils.apply(dao.getRoot());

                    return process
                        .exec("git config user.name")
                        .map(username -> process
                            .exec("git config user.email")
                            .map(mail -> User.apply(username.trim(), mail.trim()))
                            .orElse(User.apply(username)));
                }));
    }

    @Override
    public void writeTo(OutputStream os) {
        // do nothing
    }

}
