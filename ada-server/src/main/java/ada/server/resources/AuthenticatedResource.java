package ada.server.resources;

import ada.web.api.resources.about.model.AnonymousUser;
import ada.web.api.resources.about.model.AuthenticatedUser;
import ada.web.api.resources.about.model.User;
import com.google.common.collect.ImmutableSet;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

public interface AuthenticatedResource {

    default User getUser(ServerWebExchange exchange) {
        final String userId = exchange.getRequest().getHeaders().getFirst("x-user-id");
        final String rolesAllowed = exchange.getRequest().getHeaders().getFirst("x-roles-allowed");

        final Optional<String> user = Optional.ofNullable(userId);

        final ImmutableSet<String> roles = Optional
            .ofNullable(rolesAllowed)
            .map(s -> ImmutableSet.copyOf(s.split(",")))
            .orElse(ImmutableSet.of());

        return user
            .map(id -> (User) AuthenticatedUser.apply(id, roles))
            .orElse(AnonymousUser.apply(roles));
    }

}
