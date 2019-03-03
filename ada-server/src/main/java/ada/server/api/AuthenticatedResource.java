package ada.server.api;

import com.google.common.collect.ImmutableSet;
import com.ibm.ada.model.auth.Role;
import com.ibm.ada.model.auth.User;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;
import java.util.stream.Stream;

public interface AuthenticatedResource {

    default User getUser(ServerWebExchange exchange) {
        final String userId = exchange.getRequest().getHeaders().getFirst("x-user-id");
        final String rolesAllowed = exchange.getRequest().getHeaders().getFirst("x-user-roles");

        final Optional<String> user = Optional.ofNullable(userId);

        final Stream<Role> roles = Optional
            .ofNullable(rolesAllowed)
            .map(s ->
                ImmutableSet
                    .copyOf(s.split(","))
                    .stream()
                    .map(Role::apply))
            .orElse(Stream.empty());

        return user
            .map(id -> (User) User.Authenticated.apply(userId, roles))
            .orElse(User.Anonymous.apply(roles));
    }

}
