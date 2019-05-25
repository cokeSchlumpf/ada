package ada.adapters.client.modifiers;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class RequestModifiers implements RequestModifier {

    private final ImmutableList<RequestModifier> modifiers;

    public static RequestModifiers apply(Iterable<RequestModifier> modifiers) {
        return apply(ImmutableList.copyOf(modifiers));
    }

    public static RequestModifiers apply(RequestModifier... modifiers) {
        return apply(ImmutableList.copyOf(modifiers));
    }

    @Override
    public Http modifyClient(Http http) {
        for (RequestModifier m : modifiers) {
            http = m.modifyClient(http);
        }

        return http;
    }

    @Override
    public HttpRequest modifyRequest(HttpRequest request) {
        for (RequestModifier m : modifiers) {
            request = m.modifyRequest(request);
        }

        return request;
    }

}
