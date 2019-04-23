package ada.vcs.client.core.repository.api.exceptions;

import ada.vcs.client.core.repository.api.RefSpec;

public class VersionReferenceNotFoundException extends RuntimeException {

    private VersionReferenceNotFoundException(String message) {
        super(message);
    }

    public static VersionReferenceNotFoundException apply(RefSpec.VersionRef ref) {
        String message = String.format("VersionRef `%s` not found", ref.getId());
        return new VersionReferenceNotFoundException(message);
    }

}
