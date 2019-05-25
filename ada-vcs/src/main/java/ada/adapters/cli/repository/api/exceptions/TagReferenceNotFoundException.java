package ada.adapters.cli.repository.api.exceptions;

import ada.domain.dvc.values.repository.RefSpec;

public class TagReferenceNotFoundException extends RuntimeException {

    private TagReferenceNotFoundException(String message) {
        super(message);
    }

    public static TagReferenceNotFoundException apply(RefSpec.TagRef ref) {
        String message = String.format("TagRef `%s` not found", ref.getAlias().getValue());
        return new TagReferenceNotFoundException(message);
    }

}
