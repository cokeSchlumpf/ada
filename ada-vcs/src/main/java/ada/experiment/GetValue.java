package ada.experiment;

import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetValue implements CounterCommand {

    @JsonProperty("reply-to")
    private final ActorRef<Integer> replyTo;

    @JsonCreator
    public static GetValue apply(
        @JsonProperty("reply-to") ActorRef<Integer> replyTo) {
        return new GetValue(replyTo);
    }

}
