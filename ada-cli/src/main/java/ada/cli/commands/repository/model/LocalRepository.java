package ada.cli.commands.repository.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ibm.ada.model.RelativePath;
import com.ibm.ada.model.ResourceName;
import com.ibm.ada.model.repository.Commit;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode(doNotUseGetters = true)
public final class LocalRepository {

    /**
     * The name of the repository.
     */
    @Getter
    private final ResourceName name;

    /**
     * The path to the repositories metadata file
     */
    @Getter
    private final RelativePath file;

    /**
     * Status information about the remotes
     */
    private final Map<String, Remote> remotes;

    /**
     * Information about local commits.
     */
    private final Commit commit;

    private LocalRepository(ResourceName name, RelativePath file, Map<String, Remote> remotes, Commit commit) {
        this.name = name;
        this.file = file;
        this.remotes = remotes;
        this.commit = commit;
    }

    private static LocalRepository apply(ResourceName name, RelativePath file, Map<String, Remote> remotes, Commit commit) {

        if (remotes == null) {
            remotes = Maps.newHashMap();
        }

        return new LocalRepository(name, file, remotes, commit);
    }

    @JsonCreator
    public static LocalRepository apply(
        @JsonProperty("name") ResourceName name, @JsonProperty("file") RelativePath file,
        @JsonProperty("remotes") List<Remote> remotes, @JsonProperty("commit") Commit commit) {

        if (remotes == null) {
            remotes = Lists.newArrayList();
        }

        Map<String, Remote> remotesMap = Maps.newHashMap();
        remotes.forEach(r -> remotesMap.put(r.getName().getValue(), r));

        return new LocalRepository(name, file, remotesMap, commit);
    }

    public static LocalRepository apply(ResourceName name, RelativePath file) {
        return apply(name, file, Maps.newHashMap(), null);
    }

    public Optional<Commit> getCommit() {
        return Optional.ofNullable(commit);
    }

    public Stream<Remote> getRemotes() {
        return remotes.values().stream();
    }

    public LocalRepository withCommit(Commit commit) {
        if (this.commit == null || this.commit.getDistance(commit).isPresent()) {
            return apply(name, file, remotes, commit);
        } else {
            throw new IllegalArgumentException("Commit must belong to same commit chain!");
        }
    }

    public LocalRepository withRemote(Remote remote) {
        this.remotes.put(remote.getName().getValue(), remote);
        return apply(name, file, remotes, commit);
    }

    public LocalRepository withoutRemote(Remote remote) {
        return withoutRemote(remote.getName());
    }

    public LocalRepository withoutRemote(ResourceName remote) {
        this.remotes.remove(remote.getValue());
        return apply(name, file, remotes, commit);
    }

}
