package ada.vcs.shared.repository.api.exceptions;

import ada.vcs.shared.repository.api.RefSpec;

public class TagReferenceNotFoundException extends RuntimeException {

    private TagReferenceNotFoundException(String message) {
        super(message);
    }

    public static TagReferenceNotFoundException apply(RefSpec.TagRef ref) {
        String message = String.format("TagRef `%s` not found", ref.getAlias().getValue());
        return new TagReferenceNotFoundException(message);
    }

}
