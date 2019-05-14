package ada.vcs.domain.shared.repository.api.exceptions;

import ada.vcs.domain.shared.repository.api.RefSpec;

public class VersionAlreadyExistsException extends RuntimeException {

    private VersionAlreadyExistsException(String message) {
        super(message);
    }

    public static VersionAlreadyExistsException apply(RefSpec.VersionRef ref) {
        String message = String.format("VersionRef `%s` already exists in repository", ref.getId());
        return new VersionAlreadyExistsException(message);
    }

}
