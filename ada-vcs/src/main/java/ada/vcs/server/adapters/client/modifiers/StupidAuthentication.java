package ada.vcs.server.adapters.client.modifiers;

import ada.commons.util.NameFactory;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "apply")
public final class StupidAuthentication implements RequestModifier {

    private final String username;

    private final ImmutableList<String> roles;

    private final NameFactory nf;

    public static StupidAuthentication apply(String username, Iterable<String> roles) {
        return apply(username, ImmutableList.copyOf(roles), NameFactory.apply(NameFactory.Defaults.LOWERCASE_HYPHENATE));
    }

    public static StupidAuthentication apply(String username, String... roles) {
        return apply(username, ImmutableList.copyOf(roles));
    }

    @Override
    public Http modifyClient(Http http) {
        return http;
    }

    @Override
    public HttpRequest modifyRequest(HttpRequest request) {
        ArrayList<HttpHeader> headers = Lists.newArrayList(
            HttpHeader.parse("x-user-id", nf.create(username)),
            HttpHeader.parse("x-user-roles", roles.stream().map(nf::create).collect(Collectors.joining(","))));

        return request.withHeaders(headers);
    }

}