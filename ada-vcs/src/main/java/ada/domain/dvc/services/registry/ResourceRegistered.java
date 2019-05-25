package ada.domain.dvc.services.registry;

import ada.commons.util.ResourcePath;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceRegistered implements ResourceRegistryEvent {

    private final ResourcePath resource;

    @JsonCreator
    public static ResourceRegistered apply(
        @JsonProperty("resource") ResourcePath resource) {

        return new ResourceRegistered(resource);
    }

}
