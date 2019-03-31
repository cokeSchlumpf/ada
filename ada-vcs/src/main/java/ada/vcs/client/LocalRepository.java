package ada.vcs.client;

import java.nio.file.Path;

public class LocalRepository {

    /**
     * Stores the repository configurations and meta-information which will be versioned in Git.
     */
    Path root;

    /**
     * Path to local storage, should be in .gitignore
     */
    Path cache;

}
