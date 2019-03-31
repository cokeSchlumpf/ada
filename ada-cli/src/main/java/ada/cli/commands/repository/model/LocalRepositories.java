package ada.cli.commands.repository.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.ibm.ada.model.ResourceName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;

@ToString
@EqualsAndHashCode(doNotUseGetters = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalRepositories {

    /**
     * List of local repositories known in current project.
     */
    @JsonProperty
    private final Map<String, LocalRepository> repositories;

    public static LocalRepositories apply() {
        return apply(Maps.newHashMap());
    }

    @JsonCreator
    private static LocalRepositories apply(@JsonProperty("repositories") Map<String, LocalRepository> repositories) {
        return new LocalRepositories(repositories);
    }

    public Optional<LocalRepository> repository(ResourceName name) {
        return Optional.ofNullable(repositories.get(name.getValue()));
    }

    public LocalRepositories withRepository(LocalRepository repository) {
        repositories.put(repository.getName().getValue(), repository);
        return apply(repositories);
    }

    public LocalRepositories withoutRepository(LocalRepository repository) {
        return withoutRepository(repository.getName());
    }

    public LocalRepositories withoutRepository(ResourceName repository) {
        repositories.remove(repository.getValue());
        return apply(repositories);
    }

}
