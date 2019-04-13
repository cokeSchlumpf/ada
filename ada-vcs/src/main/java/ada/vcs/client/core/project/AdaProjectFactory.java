package ada.vcs.client.core.project;

import lombok.AllArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@AllArgsConstructor(staticName = "apply")
public final class AdaProjectFactory {

    public AdaProject init() {
        return init(Paths.get(System.getProperty("user.dir")));
    }

    public AdaProject init(Path where) {
        AdaProjectDAO dao = AdaProjectDAO.apply(where);
        return AdaProjectImpl.apply(dao);
    }


    public Optional<AdaProject> from(Path path) {
        if (Files.isDirectory(path) && Files.exists(path.resolve(".ada"))) {
            return Optional.of(init(path));
        } else if (path.getParent() != null) {
            return from(path.getParent());
        } else {
            return Optional.empty();
        }
    }

    public Optional<AdaProject> fromHere() {
        return from(Paths.get(System.getProperty("user.dir")));
    }

}
