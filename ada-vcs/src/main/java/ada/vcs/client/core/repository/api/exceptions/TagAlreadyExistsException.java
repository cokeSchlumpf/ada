package ada.vcs.client.core.repository.api.exceptions;

import ada.commons.util.ResourceName;

public class TagAlreadyExistsException extends RuntimeException {

    private TagAlreadyExistsException(String message) {
        super(message);
    }

    public static TagAlreadyExistsException apply(ResourceName alias) {
        String message = String.format("Tag `%s` already exists.", alias.getValue());
        return new TagAlreadyExistsException(message);
    }

}
