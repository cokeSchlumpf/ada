package ada.domain.dvc.values.repository;

import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.protocol.api.ValueObject;
import ada.domain.dvc.values.Authorization;
import ada.domain.dvc.values.GrantedAuthorization;
import ada.domain.dvc.values.UserAuthorization;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoryAuthorizations implements ValueObject {

    private final ResourceName namespace;

    private final ResourceName repository;

    private final ImmutableSet<GrantedAuthorization> authorizations;

    @JsonCreator
    public static RepositoryAuthorizations apply(
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("repository") ResourceName repository,
        @JsonProperty("authorizations") Set<GrantedAuthorization> authorizations) {

        return apply(namespace, repository, ImmutableSet.copyOf(authorizations));
    }

    public static RepositoryAuthorizations apply(
        ResourceName namespace,
        ResourceName repository,
        GrantedAuthorization... authorizations) {

        return apply(namespace, repository, Sets.newHashSet(authorizations));
    }

    @JsonIgnore
    public RepositoryAuthorizations add(GrantedAuthorization authorization) {
        Set<GrantedAuthorization> authorizations$new = Sets.newHashSet(authorizations);
        authorizations$new.add(authorization);
        return apply(namespace, repository, authorizations$new);
    }

    @JsonIgnore
    public Boolean isAuthorized(RepositoryMessage message) {
        return isPublic() || isOwner(message) || isExecutedByAuthorizedUser(message);
    }

    private boolean isExecutedByAuthorizedUser(RepositoryMessage create) {
        return authorizations
            .stream()
            .map(GrantedAuthorization::getAuthorization)
            .anyMatch(authorization -> authorization.hasAuthorization(create.getExecutor()));
    }

    private boolean isOwner(RepositoryMessage message) {
        return UserAuthorization.apply(namespace.getValue()).hasAuthorization(message.getExecutor());
    }

    private boolean isPublic() {
        return namespace.getValue().equals("public");
    }

    @JsonIgnore
    public RepositoryAuthorizations remove(Authorization authorization) {
        List<GrantedAuthorization> authorizations$new = authorizations
            .stream()
            .filter(g -> !g.getAuthorization().equals(authorization))
            .collect(Collectors.toList());

        return apply(namespace, repository, ImmutableSet.copyOf(authorizations$new));
    }

}
