package ada.adapters.cli.repository.api.exceptions;

import ada.domain.dvc.values.repository.RefSpec;

public class VersionReferenceNotFoundException extends RuntimeException {

    private VersionReferenceNotFoundException(String message) {
        super(message);
    }

    public static VersionReferenceNotFoundException apply(RefSpec.VersionRef ref) {
        String message = String.format("VersionRef `%s` not found", ref.getId());
        return new VersionReferenceNotFoundException(message);
    }

}
