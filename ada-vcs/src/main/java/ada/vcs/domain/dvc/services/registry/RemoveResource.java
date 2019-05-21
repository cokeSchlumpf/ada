package ada.vcs.domain.dvc.services.registry;

import ada.commons.util.ResourcePath;
import akka.Done;
import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RemoveResource implements ResourceRegistryCommand {

    private static final String RESOURCE = "resource";
    private static final String REPLY_TO = "reply-to";

    @JsonProperty(RESOURCE)
    private final ResourcePath resource;

    @JsonProperty(REPLY_TO)
    private final ActorRef<Done> replyTo;

    @JsonCreator
    public static RemoveResource apply(
        @JsonProperty(RESOURCE) ResourcePath resource,
        @JsonProperty(REPLY_TO) ActorRef<Done> replyTo) {

        return new RemoveResource(resource, replyTo);
    }

}
